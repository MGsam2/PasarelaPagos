package com.pasarelapagos.dto;

import com.pasarelapagos.entity.Cuenta;

import java.math.BigDecimal;

public class CuentaResponse {

    private String pan;
    private Long id;
    private Long clienteId;
    private String numeroCuenta;
    private BigDecimal saldo;
    private String estado;

    public CuentaResponse(Cuenta cuenta) {
        this.pan = cuenta.getPan();
        this.id = cuenta.getId();
        this.clienteId = cuenta.getCliente().getId();
        this.numeroCuenta = cuenta.getNumeroCuenta();
        this.saldo = cuenta.getSaldo();
        this.estado = cuenta.getEstado();
    }

    public String getPan() {
    return pan;
    }
    
    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public String getEstado() {
        return estado;
    }
}