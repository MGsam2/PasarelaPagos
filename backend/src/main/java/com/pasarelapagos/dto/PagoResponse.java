package com.pasarelapagos.dto;

import com.pasarelapagos.entity.Transaccion;

import java.math.BigDecimal;

public record PagoResponse(
        Long transaccionId,
        String estado,
        String codigoRespuesta,
        BigDecimal monto,
        String stan,
        String rrn
) {
    public PagoResponse(Transaccion transaccion) {
        this(
                transaccion.getId(),
                transaccion.getEstado(),
                transaccion.getCodigoRespuesta(),
                transaccion.getMonto(),
                transaccion.getStan(),
                null
        );
    }
}