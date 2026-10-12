package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IsoToPagoMapper {

    public PagoRequest convertir(ISOMsg mensaje) throws Exception {

        validarMensaje(mensaje);

        String pan = mensaje.getString(2);
        String montoIso = mensaje.getString(4);
        String stan = mensaje.getString(11);
        
        // DE 37 - RRN (Retrieval Reference Number)
        String rrn = mensaje.hasField(37) ? mensaje.getString(37) : null;
        
        // DE 102 - Cuenta Origen
        String cuentaOrigen = mensaje.hasField(102) ? mensaje.getString(102) : null;

        BigDecimal monto = convertirMonto(montoIso);

        return new PagoRequest(pan, monto, stan, rrn, cuentaOrigen);
    }

    private void validarMensaje(ISOMsg mensaje) throws Exception {

        if (mensaje == null) {
            throw new IllegalArgumentException(
                    "El mensaje ISO 8583 no puede ser null"
            );
        }

        if (!IsoConstants.MTI_FINANCIAL_REQUEST
                .equals(mensaje.getMTI())) {

            throw new IllegalArgumentException(
                    "MTI no válido. Se esperaba "
                            + IsoConstants.MTI_FINANCIAL_REQUEST
            );
        }

        if (!mensaje.hasField(2)) {
            throw new IllegalArgumentException(
                    "Falta DE2 - PAN"
            );
        }

        if (!mensaje.hasField(4)) {
            throw new IllegalArgumentException(
                    "Falta DE4 - monto"
            );
        }

        if (!mensaje.hasField(11)) {
            throw new IllegalArgumentException(
                    "Falta DE11 - STAN"
            );
        }
    }

    private BigDecimal convertirMonto(String montoIso) {

        if (montoIso == null || montoIso.isBlank()) {
            throw new IllegalArgumentException(
                    "El monto ISO 8583 está vacío"
            );
        }

        long centavos = Long.parseLong(montoIso);

        return BigDecimal
                .valueOf(centavos)
                .movePointLeft(2);
    }
}