package com.pasarelapagos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagoTarjetaRequest(//dto entrada web/front
        //cuando este dto llega al controlador restm lo convierte en un pagorequest para pasarselo al ruteador
        //recibe pagos desde intergaz react / http externa simplificada
        @NotBlank(message = "El PAN es obligatorio")
        String pan,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal monto,

        @NotBlank(message = "El STAN es obligatorio")
        String stan
) {
}