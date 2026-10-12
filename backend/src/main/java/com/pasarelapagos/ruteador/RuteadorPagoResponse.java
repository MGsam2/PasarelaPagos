package com.pasarelapagos.ruteador;

public record RuteadorPagoResponse(
        String banco,
        int codigoRespuesta,
        String estado
) {
}