package com.pasarelapagos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
//record clase immutable solo para transportar datos, genera automaticamente equals, hashcode y tostring
public record PagoRequest(
        @NotBlank(message = "El PAN es obligatorio")
        String pan,//primary account number, no de tarjeta
// los primeros 6,8 digitos corresponden a bin, utiliza el ruteador para saber que banco enviar la transaccion
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal monto,//valor a procesar

        @NotBlank(message = "El STAN es obligatorio")
        String stan,//umero de secuencia unico generado por la terminal o cliente por c/transaccion

        String rrn, //identificador global unico de 12 digitos asigna pasarela para rastrear la transaccion a lo largo de todo el ecosistema

        String cuentaOrigen
) {
    // Constructor compacto adicional para compatibilidad con firmas previas
    //permite crear pagorequest pasando solo pan, monto y stan asignando null a rrn y cuenta origen, ayuda 
    //mantener compatibilidad cuando la llamada viene desde el servidor tcp iso 8583
    public PagoRequest(String pan, BigDecimal monto, String stan) {
        this(pan, monto, stan, null, null);
    }
}