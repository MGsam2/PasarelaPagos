package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoResponse;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.entity.Transaccion;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IsoResponseMapperTest {

    @Test
    void debeConvertirPagoARespuestaIso0210() throws Exception {

        IsoMessageFactory factory = new IsoMessageFactory();

        ISOMsg solicitud = new ISOMsg();

        solicitud.setPackager(
                factory.getPackager()
        );

        solicitud.setMTI(
                IsoConstants.MTI_FINANCIAL_REQUEST
        );

        solicitud.set(2, "4111111111111111");
        solicitud.set(3, "000000");
        solicitud.set(4, "000000003000");
        solicitud.set(11, "123456");
        solicitud.set(41, "ATM00001");
        solicitud.set(49, "320");

        Cuenta cuenta = new Cuenta();

        Transaccion transaccion = new Transaccion(
                cuenta,
                new BigDecimal("30.00"),
                "PAGO",
                "APROBADA",
                "00",
                "123456",
                LocalDateTime.now()
        );

        PagoResponse pagoResponse =
                new PagoResponse(transaccion);

        IsoResponseMapper mapper =
                new IsoResponseMapper(factory);

        ISOMsg respuesta =
                mapper.convertir(
                        pagoResponse,
                        solicitud
                );

        assertNotNull(respuesta);

        assertEquals(
                "0210",
                respuesta.getMTI()
        );

        assertEquals(
                "4111111111111111",
                respuesta.getString(2)
        );

        assertEquals(
                "000000",
                respuesta.getString(3)
        );

        assertEquals(
                "000000003000",
                respuesta.getString(4)
        );

        assertEquals(
                "123456",
                respuesta.getString(11)
        );

        assertEquals(
                "00",
                respuesta.getString(39)
        );

        assertEquals(
                "ATM00001",
                respuesta.getString(41)
        );

        assertEquals(
                "320",
                respuesta.getString(49)
        );

        System.out.println("========================================");
        System.out.println("REST -> ISO 8583");
        System.out.println("========================================");
        System.out.println("MTI: " + respuesta.getMTI());
        System.out.println("DE2 PAN: " + respuesta.getString(2));
        System.out.println("DE3 PROCESSING CODE: " + respuesta.getString(3));
        System.out.println("DE4 MONTO: " + respuesta.getString(4));
        System.out.println("DE11 STAN: " + respuesta.getString(11));
        System.out.println("DE39 RESPONSE CODE: " + respuesta.getString(39));
        System.out.println("DE41 TERMINAL: " + respuesta.getString(41));
        System.out.println("DE49 MONEDA: " + respuesta.getString(49));
        System.out.println("========================================");
        System.out.println("CONVERSION PagoResponse -> ISO 0210 EXITOSA");
    }
}