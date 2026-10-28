package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoResponse;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;
/*MAPEADOR DE SALIDA
construye respuesta iso 8583 MTI 0210 a partir de resultado
procesado pro el core bancario PagoResponse o Codigo de error */
@Component
public class IsoResponseMapper {

    private final IsoMessageFactory isoMessageFactory;

    public IsoResponseMapper(IsoMessageFactory isoMessageFactory) {
        this.isoMessageFactory = isoMessageFactory;
    }
/*METODO PRINCIPAL convierte la respusta exitosa o rechazada del sistema
en una trama iso 8583 0210 vinculada a la solicitud original */
    public ISOMsg convertir(//debe devolver un objeto de tipo isomsg
            PagoResponse pagoResponse,//contiene el resultado del procesamiento del pago
            ISOMsg mensajeSolicitud) throws Exception {

        if (pagoResponse == null) {
            throw new IllegalArgumentException(
                    "El PagoResponse no puede ser null"
            );
        }

        if (mensajeSolicitud == null) {
            throw new IllegalArgumentException(
                    "El mensaje ISO de solicitud no puede ser null"
            );
        }

        ISOMsg respuesta = new ISOMsg();
        //asigna la plantilla de empaquetado jPOS
        respuesta.setPackager(
                isoMessageFactory.getPackager()
        );

        // MTI 0210 = Identificador de respuesta a transaccion financiera
        respuesta.setMTI(
                IsoConstants.MTI_FINANCIAL_RESPONSE
        );
        //COPIA DE SEGURIDAD Y CONCILIACION
        // Copiamos los campos de la solicitud hacia la respuesta, mantiene la trazabilidad
        if (mensajeSolicitud.hasField(2)) {
            respuesta.set(2, mensajeSolicitud.getString(2));//DE2 tarjeta PAN
        }

        if (mensajeSolicitud.hasField(3)) {
            respuesta.set(3, mensajeSolicitud.getString(3));//DE3 Processing Code
        }

        if (mensajeSolicitud.hasField(4)) {
            respuesta.set(4, mensajeSolicitud.getString(4));//DE4 Monto
        }

        // DE 11 STAN Auditoria de transaccion
        if (mensajeSolicitud.hasField(11)) {
            respuesta.set(11, mensajeSolicitud.getString(11));
        } else if (pagoResponse.stan() != null) {
            respuesta.set(11, pagoResponse.stan());
        }

        // Código de respuesta, campo clave DE39 cod de respuesta financiera 00, 14. 51, 96
        respuesta.set(
                39,
                pagoResponse.codigoRespuesta()
        );

        if (mensajeSolicitud.hasField(41)) {
            respuesta.set(41, mensajeSolicitud.getString(41));//DE41 IDterminal /POS
        }

        if (mensajeSolicitud.hasField(49)) {
            respuesta.set(49, mensajeSolicitud.getString(49));//DE 49 Moneda ejem 320 para quetzal
        }

        return respuesta;
    }
    /*METODOS AUXILIARES DE ERROR
    Genera respuestas MTI 0210 cuando ocurre falla critica
    timeout o excepcion antes de llegar a procesar la transaccion */
    public ISOMsg crearRespuestaError(
            ISOMsg mensajeSolicitud,
            String codigoRespuesta) throws Exception {

        if (mensajeSolicitud == null) {
            throw new IllegalArgumentException(
                    "El mensaje ISO de solicitud no puede ser null"
            );
        }

        if (codigoRespuesta == null
                || codigoRespuesta.length() != 2) {

            throw new IllegalArgumentException(
                    "El código de respuesta ISO debe tener 2 caracteres"
            );
        }

        ISOMsg respuesta = new ISOMsg();//instancia de objeto, llamando al constructor

        respuesta.setPackager(
                isoMessageFactory.getPackager()
        );
        //MTI 0210
        respuesta.setMTI(
                IsoConstants.MTI_FINANCIAL_RESPONSE
        );
        /*ser replica los encabezados de la solicitud original
        //Si la solicitud original tiene el campo 2, copia su valor al campo 2 de la respuesta.
        hasfield comprueba y getstring obtiene el valor y set copia al nuevo mensaje*/
        if (mensajeSolicitud.hasField(2)) {
            respuesta.set(
                    2,
                    mensajeSolicitud.getString(2)
            );
        }

        if (mensajeSolicitud.hasField(3)) {
            respuesta.set(
                    3,
                    mensajeSolicitud.getString(3)
            );
        }

        if (mensajeSolicitud.hasField(4)) {
            respuesta.set(
                    4,
                    mensajeSolicitud.getString(4)
            );
        }

        if (mensajeSolicitud.hasField(11)) {
            respuesta.set(
                    11,
                    mensajeSolicitud.getString(11)
            );
        }
        //se asigna el codigo de error correspondiente en el DE 38 ejemplo 96 = System error
        respuesta.set(
                39,
                codigoRespuesta
        );

        if (mensajeSolicitud.hasField(41)) {
            respuesta.set(
                    41,
                    mensajeSolicitud.getString(41)
            );
        }

        if (mensajeSolicitud.hasField(49)) {
            respuesta.set(
                    49,
                    mensajeSolicitud.getString(49)
            );
        }

        return respuesta;
    }

    public ISOMsg crearRespuestaDesdeCodigo(
            ISOMsg mensajeSolicitud,
            String codigoRespuesta) throws Exception {

        if (mensajeSolicitud == null) {
            throw new IllegalArgumentException(
                    "El mensaje ISO de solicitud no puede ser null"
            );
        }

        if (codigoRespuesta == null
                || codigoRespuesta.length() != 2) {

            throw new IllegalArgumentException(
                    "El código de respuesta ISO debe tener 2 caracteres"
            );
        }

        ISOMsg respuesta = new ISOMsg();

        respuesta.setPackager(
                isoMessageFactory.getPackager()
        );

        respuesta.setMTI(
                IsoConstants.MTI_FINANCIAL_RESPONSE
        );

        if (mensajeSolicitud.hasField(2)) {
            respuesta.set(
                    2,
                    mensajeSolicitud.getString(2)
            );
        }

        if (mensajeSolicitud.hasField(3)) {
            respuesta.set(
                    3,
                    mensajeSolicitud.getString(3)
            );
        }

        if (mensajeSolicitud.hasField(4)) {
            respuesta.set(
                    4,
                    mensajeSolicitud.getString(4)
            );
        }

        if (mensajeSolicitud.hasField(11)) {
            respuesta.set(
                    11,
                    mensajeSolicitud.getString(11)
            );
        }

        respuesta.set(
                39,
                codigoRespuesta
        );

        if (mensajeSolicitud.hasField(41)) {
            respuesta.set(
                    41,
                    mensajeSolicitud.getString(41)
            );
        }

        if (mensajeSolicitud.hasField(49)) {
            respuesta.set(
                    49,
                    mensajeSolicitud.getString(49)
            );
        }

        return respuesta;
    }
}

/*"El IsoResponseMapper es responsable de construir la respuesta ISO 8583 (MTI 0210). 
Implementa la técnica de Mirroring (Eco), copiando de forma segura los Data Elements críticos 
de la solicitud original (PAN, STAN, Monto) hacia el nuevo objeto de respuesta para garantizar 
la conciliación exacta en el terminal POS. Además, actúa como un componente de resiliencia al 
proveer métodos de construcción de respuestas de error (como el código 96), garantizando que el 
POS nunca se quede esperando en caso de un fallo interno en la base de datos o en la red." */

/*"El IsoResponseMapper es el encargado de construir el mensaje de respuesta 0210. 
Mantiene la trazabilidad copiando los datos de la solicitud original (tarjeta, monto, STAN y terminal) 
y asigna el resultado final en el Data Element 39 (DE 39), devolviendo 00 si fue aprobada o códigos como 51 o 96 si fue rechazada." */