package com.pasarelapagos.dto;

import com.pasarelapagos.entity.Transaccion;

import java.math.BigDecimal;

public class PagoResponse {

    private Long transaccionId;
    private String estado;
    private String codigoRespuesta;
    private BigDecimal monto;
    private String stan;

    public PagoResponse(Transaccion transaccion) {
        this.transaccionId = transaccion.getId();
        this.estado = transaccion.getEstado();
        this.codigoRespuesta = transaccion.getCodigoRespuesta();
        this.monto = transaccion.getMonto();
        this.stan = transaccion.getStan();
    }

    public Long getTransaccionId() {
        return transaccionId;
    }

    public String getEstado() {
        return estado;
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getStan() {
        return stan;
    }
}