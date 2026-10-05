package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoResponse;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Component
public class IsoResponseMapper {

    private final IsoMessageFactory isoMessageFactory;

    public IsoResponseMapper(IsoMessageFactory isoMessageFactory) {
        this.isoMessageFactory = isoMessageFactory;
    }

    public ISOMsg convertir(
            PagoResponse pagoResponse,
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

        respuesta.setPackager(
                isoMessageFactory.getPackager()
        );

        // 0210 = Financial Transaction Response
        respuesta.setMTI(
                IsoConstants.MTI_FINANCIAL_RESPONSE
        );

        // Copiamos datos importantes de la solicitud
        if (mensajeSolicitud.hasField(2)) {
            respuesta.set(2, mensajeSolicitud.getString(2));
        }

        if (mensajeSolicitud.hasField(3)) {
            respuesta.set(3, mensajeSolicitud.getString(3));
        }

        if (mensajeSolicitud.hasField(4)) {
            respuesta.set(4, mensajeSolicitud.getString(4));
        }

        // STAN
        if (mensajeSolicitud.hasField(11)) {
            respuesta.set(11, mensajeSolicitud.getString(11));
        } else if (pagoResponse.getStan() != null) {
            respuesta.set(11, pagoResponse.getStan());
        }

        // Código de respuesta
        respuesta.set(
                39,
                pagoResponse.getCodigoRespuesta()
        );

        if (mensajeSolicitud.hasField(41)) {
            respuesta.set(41, mensajeSolicitud.getString(41));
        }

        if (mensajeSolicitud.hasField(49)) {
            respuesta.set(49, mensajeSolicitud.getString(49));
        }

        return respuesta;
    }

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
