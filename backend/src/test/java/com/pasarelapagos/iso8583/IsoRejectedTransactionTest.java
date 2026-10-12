package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IsoRejectedTransactionTest {

    @Autowired
    private IsoTransactionService isoTransactionService;

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @Test
    void debeRechazarPagoPorFondosInsuficientes() throws Exception {
        // PAN de Banco A
        String pan = "4111111111111111";
        // Enviamos un monto intencionalmente excesivo (ej. Q 999,999.00 = 99999900 centavos)
        String montoCentavosExcesivo = "9999990000"; 
        String stan = "654321";

        ISOMsg solicitud = isoMessageFactory.crearMensajePago(
                pan,
                IsoConstants.PROCESSING_CODE_PURCHASE,
                montoCentavosExcesivo,
                stan,
                "TERM0001",
                "MERCHANT001"
        );

        byte[] mensajeBytes = isoParser.pack(solicitud);
        byte[] respuestaBytes = isoTransactionService.procesar(mensajeBytes);

        ISOMsg respuesta = isoParser.unpack(respuestaBytes);

        assertNotNull(respuesta);
        assertEquals("0210", respuesta.getMTI());
        // Verificamos que devuelva el código ISO 51 (Fondos insuficientes)
        assertEquals(IsoConstants.RESPONSE_INSUFFICIENT_FUNDS, respuesta.getString(39));
    }
}