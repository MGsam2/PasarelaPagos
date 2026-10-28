package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;//libreria pos Procesar bytes y leer mapas de bits
import org.springframework.stereotype.Component;

/*Esta clase es el "TRADUCTOR Y EMPACADOR" de la pasarela.
 Convierte el texto/bytes que vienen del cable de red TCP 
 en un objeto manipulable en Java, y viceversa.*/
@Component
public class IsoParser {

    private final IsoMessageFactory isoMessageFactory;

    public IsoParser(IsoMessageFactory isoMessageFactory) {
        this.isoMessageFactory = isoMessageFactory;
    }

    /*DESEMPAQUETAR (Recibir del cliente/POS)
    Toma los datos crudos (bytes) que llegaron por el puerto 9090
    y los "desarma" campo por campo (Tarjeta, Monto, STAN).*/
    public ISOMsg unpack(byte[] mensaje) throws Exception {

        ISOMsg isoMsg = new ISOMsg();

        // Le enseñamos a la librería la plantilla/reglas de cómo viene el mensaje
        isoMsg.setPackager(//guarda en ram la plantilla siendo mas rapido 
                isoMessageFactory.getPackager()
        );

        // Desarma el paquete de bytes
        isoMsg.unpack(mensaje);

        return isoMsg;
    }

    /**
     * EMPAQUETAR (Responder al cliente/POS)
     * Toma el mensaje de respuesta de Java (0210) y lo "vuelve a armar"
     * en un canal de bytes listo para enviarlo de vuelta por el cable TCP.
     */
    public byte[] pack(ISOMsg mensaje) throws Exception {
        return mensaje.pack();
    }
}



/*El "Packager" (La Regla de Oro de ISO 8583):

En el método unpack, esta línea es crítica: isoMsg.setPackager(...). 
Un paquete de bytes de ISO 8583 no dice de qué tamaño es cada campo por defecto. 
Es el Packager (generalmente un archivo XML) quien le dice a jPOS: 
"El campo 2 es de tamaño variable, el campo 4 es numérico de 12 posiciones, 
y el campo 39 son 2 caracteres alfanuméricos". Sin el Packager, es imposible traducir la trama.

Alto Rendimiento (Inyección de Dependencia):

No estmos creando el Packager desde cero en cada transacción. Lo estás pidiendo a 
la clase IsoMessageFactory. Crear el Packager requiere leer un archivo XML, lo cual es muy lento. 
Al inyectarlo y reutilizarlo, lograste que el unpack ocurra en nanosegundos, clave para mis 358,017 TPS. */