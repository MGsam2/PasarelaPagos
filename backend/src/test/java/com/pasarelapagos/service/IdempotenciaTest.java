package com.pasarelapagos.service;

import com.pasarelapagos.iso8583.IsoConstants;
import com.pasarelapagos.iso8583.IsoMessageFactory;
import com.pasarelapagos.iso8583.IsoParser;
import com.pasarelapagos.iso8583.IsoTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdempotenciaTest {

    @Autowired
    private IsoTransactionService isoTransactionService;

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @Test
    void deberiaRetornarMismaRespuestaAntePeticionDuplicada() throws Exception {
        var isoMsg = isoMessageFactory.crearMensajePago(
                "4111111111111111",
                IsoConstants.PROCESSING_CODE_PURCHASE,
                "000000000500", // Q5.00
                "888777",
                "TERM0001",
                "MERCHANT001"
        );
        byte[] tramaEnvio = isoParser.pack(isoMsg);

        // Primera llamada
        byte[] res1Bytes = isoTransactionService.procesar(tramaEnvio);
        var res1 = isoParser.unpack(res1Bytes);

        // Segunda llamada con la misma trama (mismo STAN)
        byte[] res2Bytes = isoTransactionService.procesar(tramaEnvio);
        var res2 = isoParser.unpack(res2Bytes);

        assertEquals(res1.getString(39), res2.getString(39));
    }
}