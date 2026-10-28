package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoResponse;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

/**
 * Mapeador de salida para la construcción de mensajes ISO 8583 (MTI 0210).
 * Aplica técnicas de Eco (Mirroring) para garantizar trazabilidad y conciliación
 * exacta con el adquirente / POS original (DE 2, 3, 4, 7, 11, 12, 13, 37, 41, 42, 49).
 */
@Component
public class IsoResponseMapper {

    private final IsoMessageFactory isoMessageFactory;

    public IsoResponseMapper(IsoMessageFactory isoMessageFactory) {
        this.isoMessageFactory = isoMessageFactory;
    }

    /**
     * Construye una trama de respuesta 0210 a partir de un {@link PagoResponse} procesado.
     */
    public ISOMsg convertir(PagoResponse pagoResponse, ISOMsg mensajeSolicitud) throws Exception {
        if (pagoResponse == null) {
            throw new IllegalArgumentException("El PagoResponse no puede ser null");
        }
        if (mensajeSolicitud == null) {
            throw new IllegalArgumentException("El mensaje ISO de solicitud no puede ser null");
        }

        ISOMsg respuesta = crearBaseRespuesta0210(mensajeSolicitud);

        // DE 39: Código de respuesta financiera (00, 14, 51, 96, etc.)
        respuesta.set(39, pagoResponse.codigoRespuesta());

        // Asegura STAN en DE 11
        if (!respuesta.hasField(11) && pagoResponse.stan() != null) {
            respuesta.set(11, pagoResponse.stan());
        }

        return respuesta;
    }

    /**
     * Construye una respuesta ISO 0210 ante errores del sistema o validaciones fallidas.
     */
    public ISOMsg crearRespuestaError(ISOMsg mensajeSolicitud, String codigoRespuesta) throws Exception {
        return crearRespuestaDesdeCodigo(mensajeSolicitud, codigoRespuesta);
    }

    /**
     * Construye una respuesta ISO 0210 reflejando los datos de la solicitud y asignando DE 39.
     */
    public ISOMsg crearRespuestaDesdeCodigo(ISOMsg mensajeSolicitud, String codigoRespuesta) throws Exception {
        if (mensajeSolicitud == null) {
            throw new IllegalArgumentException("El mensaje ISO de solicitud no puede ser null");
        }
        if (codigoRespuesta == null || codigoRespuesta.length() != 2) {
            throw new IllegalArgumentException("El código de respuesta ISO debe tener exactamente 2 caracteres");
        }

        ISOMsg respuesta = crearBaseRespuesta0210(mensajeSolicitud);
        respuesta.set(39, codigoRespuesta);
        return respuesta;
    }

    /**
     * Método centralizado para construir la trama MTI 0210 replicando (Mirroring)
     * los Data Elements de contexto requeridos por el estándar bancario.
     */
    private ISOMsg crearBaseRespuesta0210(ISOMsg solicitud) throws Exception {
        ISOMsg respuesta = new ISOMsg();
        respuesta.setPackager(isoMessageFactory.getPackager());
        respuesta.setMTI(IsoConstants.MTI_FINANCIAL_RESPONSE); // MTI 0210

        // Eco de campos estándar de la transacción original
        copiarCampoSiExiste(solicitud, respuesta, 2);  // PAN
        copiarCampoSiExiste(solicitud, respuesta, 3);  // Processing Code
        copiarCampoSiExiste(solicitud, respuesta, 4);  // Monto
        copiarCampoSiExiste(solicitud, respuesta, 7);  // Transmission Date/Time
        copiarCampoSiExiste(solicitud, respuesta, 11); // STAN
        copiarCampoSiExiste(solicitud, respuesta, 12); // Local Time
        copiarCampoSiExiste(solicitud, respuesta, 13); // Local Date
        copiarCampoSiExiste(solicitud, respuesta, 37); // RRN
        copiarCampoSiExiste(solicitud, respuesta, 41); // Terminal ID
        copiarCampoSiExiste(solicitud, respuesta, 42); // Merchant ID
        copiarCampoSiExiste(solicitud, respuesta, 49); // Currency Code (320 Quetzales)

        return respuesta;
    }

    private void copiarCampoSiExiste(ISOMsg origen, ISOMsg destino, int campo) throws Exception {
        if (origen.hasField(campo)) {
            destino.set(campo, origen.getString(campo));
        }
    }
}
