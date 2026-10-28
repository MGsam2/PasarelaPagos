package com.pasarelapagos.ruteador;

/*DTO RECORD DE RESPUESTA DEL RUTEADOR:
 Estructura de datos inmutable enviada como respuesta JSON al cliente REST.
 
 @param banco  ("BANCO_A", "BANCO_B", "DESCONOCIDO")
 @param codigoRespuesta Código de resultado bancario (0 = Exitoso, 14, 51, etc.)
 @param estado Texto de estado ("APROBADA" o "RECHAZADA") */
public record RuteadorPagoResponse(
        String banco,
        int codigoRespuesta,
        String estado
) {
}