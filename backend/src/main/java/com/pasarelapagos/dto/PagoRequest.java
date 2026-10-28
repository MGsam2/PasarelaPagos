package com.pasarelapagos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO inmutable (Java Record) para transporte transaccional de pagos.
 * Representa los Data Elements normalizados extraídos de la trama ISO 8583 (MTI 0200).
 */
public record PagoRequest(
        @NotBlank(message = "El PAN es obligatorio")
        String pan, // DE 2 - Primary Account Number (Tarjeta)

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal monto, // DE 4 - Monto en formato monetario normalizado

        @NotBlank(message = "El STAN es obligatorio")
        String stan, // DE 11 - System Trace Audit Number (Auditoría)

        String rrn, // DE 37 - Retrieval Reference Number (12 caracteres)

        String cuentaOrigen, // DE 102 - Cuenta de cargo del tarjetahabiente

        String processingCode // DE 3 - Processing Code (6 dígitos, ej. 000000 para compra)
) {

    // Constructor para llamadas directas simplificadas
    public PagoRequest(String pan, BigDecimal monto, String stan) {
        this(pan, monto, stan, null, null, "000000");
    }

    // Constructor previo para compatibilidad hacia atrás
    public PagoRequest(String pan, BigDecimal monto, String stan, String rrn, String cuentaOrigen) {
        this(pan, monto, stan, rrn, cuentaOrigen, "000000");
    }
}
