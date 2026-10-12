package com.pasarelapagos.dto;

import com.pasarelapagos.entity.Cuenta;

import java.math.BigDecimal;

public record CuentaResponse(
        String pan,
        Long id,
        Long clienteId,
        String numeroCuenta,
        BigDecimal saldo,
        String estado
) {
    public CuentaResponse(Cuenta cuenta) {
        this(
                cuenta.getPan(),
                cuenta.getId(),
                cuenta.getCliente().getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                cuenta.getEstado()
        );
    }
}