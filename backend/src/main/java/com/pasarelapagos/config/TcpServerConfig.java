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

/*CONFIGURACIÓN DEL SERVIDOR TCP (LA PUERTA INDUSTRIAL):
 Levanta un servidor de Sockets de red utilizando Spring Integration 
 para recibir las conexiones físicas de los datáfonos (POS) vía ISO 8583.*/
@Configuration
public class TcpServerConfig {

    // Puerto de escucha inyectado desde properties (por defecto 9090)
    @Value("${tcp.server.port:9090}")
    private int port;

    /*FÁBRICA DE CONEXIONES NIO (Non-Blocking I/O):
     El motor de red de alto rendimiento que acepta las conexiones entrantes.*/
    @Bean
    public TcpNioServerConnectionFactory tcpServerConnectionFactory() {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(port);
        
        // EL SECRETO DEL PROTOCOLO ISO:
        // Configura el servidor para leer los primeros 2 bytes de cada mensaje, 
        // los cuales indican la longitud total de la trama (estándar bancario).
        ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer(2);
        factory.setSerializer(serializer);
        factory.setDeserializer(serializer);
        
        // Mantiene la conexión TCP abierta (Keep-Alive) para múltiples transacciones
        factory.setSingleUse(false);

        // LA MAGIA DE JAVA 21: VIRTUAL THREADS
        // Asigna un hilo virtual súper ligero por cada conexión/tarea entrante,
        // permitiendo soportar cientos de miles de conexiones sin saturar la RAM.
        factory.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());

        return factory;
    }

    /*CANAL DE ENTRADA:
     Tubería interna en memoria que transporta el mensaje desde el puerto de red hacia la lógica de negocio.*/
    @Bean
    public MessageChannel tcpInputChannel() {
        return new DirectChannel();
    }

    /*GATEWAY TCP (La Aduana):
     Conecta el servidor físico de Sockets (ConnectionFactory) con el canal interno de Spring (MessageChannel).*/
    @Bean
    public TcpInboundGateway tcpInboundGateway(
            TcpNioServerConnectionFactory tcpServerConnectionFactory,
            MessageChannel tcpInputChannel) {

        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(tcpServerConnectionFactory);
        gateway.setRequestChannel(tcpInputChannel);
        return gateway;
    }

    /*MANEJADOR DE MENSAJES ISO (@ServiceActivator):
     Intercepta los bytes crudos que entran por el canal, los envía al IsoTransactionService 
     y devuelve la respuesta empaquetada (ISO 0210) de regreso por el mismo túnel TCP.*/
    @Bean
    @ServiceActivator(inputChannel = "tcpInputChannel")
    public MessageHandler tcpMessageHandler(IsoTransactionService isoTransactionService) {
        return message -> {
            byte[] payload = (byte[]) message.getPayload();
            try {
                // 1. Procesa la trama ISO (traducción, enrutamiento, cobro)
                byte[] respuesta = isoTransactionService.procesar(payload);
                
                // 2. Obtiene el canal de respuesta (la ruta de regreso al POS físico)
                MessageChannel replyChannel = (MessageChannel) message.getHeaders().getReplyChannel();
                
                if (replyChannel != null) {
                    // 3. Envía los bytes de respuesta ISO 0210 de vuelta a la red
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
/*"El TcpServerConfig expone el punto de entrada industrial de la pasarela mediante Spring 
Integration. Para soportar el estrés masivo, configuré el servidor sobre NIO (Non-Blocking I/O) 
propulsado por los Virtual Threads de Java 21, lo que erradica el cuello de botella de los hilos 
del sistema operativo. A nivel de protocolo, implementé un deserializador de cabecera de 2 bytes 
(ByteArrayLengthHeaderSerializer), cumpliendo estrictamente con el estándar de empaquetado ISO 8583 
sobre redes TCP/IP. Finalmente, un Gateway convierte los flujos de bytes crudos en mensajes de Spring,
 entregándolos al orquestador central y devolviendo la respuesta financiera a la terminal origen 
 por el mismo túnel TCP de forma asíncrona." */