package com.pasarelapagos.iso8583;

import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.repository.CuentaRepository;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IsoTransactionServiceTest {

    @Autowired
    private IsoTransactionService isoTransactionService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @Test
    void debeProcesarTransaccionIsoCompleta() throws Exception {

        String pan = "4111111111111111";

        /*
         * 1. Obtener saldo antes de la operación.
         */
        Cuenta cuentaAntes = cuentaRepository.findByPan(pan)
                .orElseThrow(() ->
                        new AssertionError(
                                "No existe una cuenta con el PAN del test"
                        )
                );

        BigDecimal saldoAntes =
                cuentaAntes.getSaldo();

        /*
         * 2. Crear mensaje ISO 0200.
         */
        ISOMsg solicitud =
                isoMessageFactory.crearMensajePago(
                        pan,
                        IsoConstants.PROCESSING_CODE_PURCHASE,
                        "000000003000",
                        "234567",
                        "ATM00001",
                        "COMERCIO000001"
                );

        /*
         * DE70 = 001 utiliza el bitmap secundario.
         */
        solicitud.set(70, "001");

        /*
         * 3. Empaquetar ISO 0200.
         */
        byte[] mensajeIso =
                solicitud.pack();

        /*
         * 4. Ejecutar el flujo completo.
         */
        byte[] respuestaIso =
                isoTransactionService.procesar(
                        mensajeIso
                );

        assertNotNull(respuestaIso);
        assertTrue(respuestaIso.length > 0);

        /*
         * 5. Desempaquetar ISO 0210.
         */
        ISOMsg respuesta =
                isoParser.unpack(respuestaIso);

        /*
         * 6. Validar respuesta ISO.
         */
        assertEquals(
                IsoConstants.MTI_FINANCIAL_RESPONSE,
                respuesta.getMTI()
        );

        assertEquals(
                pan,
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
                "234567",
                respuesta.getString(11)
        );

        assertEquals(
                IsoConstants.RESPONSE_APPROVED,
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

        /*
         * 7. Verificar que PostgreSQL realmente
         *    modificó el saldo.
         */
        Cuenta cuentaDespues =
                cuentaRepository.findByPan(pan)
                        .orElseThrow();

        BigDecimal saldoDespues =
                cuentaDespues.getSaldo();

        assertEquals(
                saldoAntes.subtract(
                        new BigDecimal("30.00")
                ),
                saldoDespues
        );

        System.out.println("========================================");
        System.out.println("FLUJO ISO 8583 COMPLETO");
        System.out.println("========================================");
        System.out.println("PAN: " + pan);
        System.out.println("SALDO ANTES: Q" + saldoAntes);
        System.out.println("MONTO: Q30.00");
        System.out.println("SALDO DESPUES: Q" + saldoDespues);
        System.out.println("STAN: " + respuesta.getString(11));
        System.out.println("MTI RESPUESTA: " + respuesta.getMTI());
        System.out.println("DE39: " + respuesta.getString(39));
        System.out.println("========================================");
        System.out.println("TRANSACCION ISO COMPLETA EXITOSA");
    }
}