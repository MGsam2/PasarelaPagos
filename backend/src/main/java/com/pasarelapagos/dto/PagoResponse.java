package com.pasarelapagos.dto;

import com.pasarelapagos.entity.Transaccion;
import java.math.BigDecimal;
//asocia el transaccionid generado por la BD con el stan y rrn recibido, permite quel a termial
//tcp o el cliente rest confirme la transaccion aprobada 00 o rechazada 14, 51 etc
public record PagoResponse(
        Long transaccionId,
        String estado,
        String codigoRespuesta,
        BigDecimal monto,
        String stan,
        String rrn
) {//mapeo directo desde entidad transaccion con constructor secundario
    //facilita la conversion de entidad persistida en PSQL directo a un dto respuesta para enviar al cliente
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