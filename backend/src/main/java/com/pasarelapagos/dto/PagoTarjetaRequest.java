package com.pasarelapagos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagoTarjetaRequest(
        @NotBlank(message = "El PAN es obligatorio")
        String pan,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal monto,

        @NotBlank(message = "El STAN es obligatorio")
        String stan
) {
}