package com.pasarelapagos.config;

import com.pasarelapagos.iso8583.IsoTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Configuración del Servidor TCP de Ingesta Financiera ISO 8583.
 * Implementa Non-Blocking I/O (NIO) asistido por Virtual Threads (Project Loom)
 * y protocolo de longitud de cabecera de 2 bytes (ByteArrayLengthHeaderSerializer).
 */
@Configuration
public class TcpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(TcpServerConfig.class);

    @Value("${tcp.server.port:9090}")
    private int port;

    /**
     * Fábrica de conexiones TCP NIO no bloqueante asistida por Virtual Threads de Java 21+.
     */
    @Bean
    public TcpNioServerConnectionFactory tcpServerConnectionFactory() {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(port);

        // Cabecera estándar de 2 bytes de longitud de trama (estándar bancario)
        ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer(2);
        factory.setSerializer(serializer);
        factory.setDeserializer(serializer);

        // Mantiene conexiones TCP persistentes (Keep-Alive)
        factory.setSingleUse(false);

        // Concurrencia de alto rendimiento mediante hilos virtuales ligeros
        factory.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());

        log.info("Servidor TCP ISO 8583 inicializado en el puerto {} con soporte para Virtual Threads", port);
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

    /**
     * Manejador de eventos TCP (@ServiceActivator): delega el procesamiento al pipeline ISO
     * y retorna la respuesta MTI 0210 por el canal de retorno del socket sin bloqueos.
     */
    @Bean
    @ServiceActivator(inputChannel = "tcpInputChannel")
    public MessageHandler tcpMessageHandler(IsoTransactionService isoTransactionService) {
        return message -> {
            byte[] payload = (byte[]) message.getPayload();
            try {
                // 1. Procesa la trama ISO a través del pipeline transaccional
                byte[] respuesta = isoTransactionService.procesar(payload);

                // 2. Obtiene el canal de respuesta hacia el terminal POS
                MessageChannel replyChannel = (MessageChannel) message.getHeaders().getReplyChannel();

                if (replyChannel != null) {
                    replyChannel.send(
                            MessageBuilder.withPayload(respuesta)
                                    .copyHeaders(message.getHeaders())
                                    .build()
                    );
                }
            } catch (Exception e) {
                log.error("Error en Servidor TCP ISO al procesar trama: {}", e.getMessage(), e);
            }
        };
    }
}
