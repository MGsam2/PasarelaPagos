package com.pasarelapagos.config;

import com.pasarelapagos.iso8583.IsoTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.inbound.TcpInboundGateway;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.Executors;

@Configuration
public class TcpServerConfig {

    @Value("${tcp.server.port:9090}")
    private int port;

    @Bean
    public TcpNioServerConnectionFactory tcpServerConnectionFactory() {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(port);
        
        ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer(2);
        factory.setSerializer(serializer);
        factory.setDeserializer(serializer);
        factory.setSingleUse(false);

        // Virtual Threads para concurrencia no bloqueante
        factory.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());

        return factory;
    }

    @Bean
    public MessageChannel tcpInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpInboundGateway tcpInboundGateway(
            TcpNioServerConnectionFactory tcpServerConnectionFactory,
            MessageChannel tcpInputChannel) {

        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(tcpServerConnectionFactory);
        gateway.setRequestChannel(tcpInputChannel);
        return gateway;
    }

    @Bean
    @ServiceActivator(inputChannel = "tcpInputChannel")
    public MessageHandler tcpMessageHandler(IsoTransactionService isoTransactionService) {
        return message -> {
            byte[] payload = (byte[]) message.getPayload();
            try {
                byte[] respuesta = isoTransactionService.procesar(payload);
                MessageChannel replyChannel = (MessageChannel) message.getHeaders().getReplyChannel();
                
                if (replyChannel != null) {
                    replyChannel.send(
                            MessageBuilder.withPayload(respuesta)
                                    .copyHeaders(message.getHeaders())
                                    .build()
                    );
                }
            } catch (Exception e) {
                System.err.println("Error en Servidor TCP ISO: " + e.getMessage());
            }
        };
    }
}