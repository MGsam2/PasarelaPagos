package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IsoBancoBTest {

    @Autowired
    private IsoTransactionService isoTransactionService;

    @Autowired
    private IsoParser isoParser;

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private JdbcTemplate bancoBJdbcTemplate;

    @Test
    void debeProcesarPagoEnBancoB() throws Exception {

        String pan = "5222222222222222";
        BigDecimal monto = new BigDecimal("30.00");
        String stan = "777777";

        BigDecimal saldoInicial =
                bancoBJdbcTemplate.queryForObject(
                        """
                        SELECT saldo
                        FROM cuentas
                        WHERE pan = ?
                        """,
                        BigDecimal.class,
                        pan
                );

        assertNotNull(saldoInicial);

        System.out.println("========================================");
        System.out.println("PRUEBA BANCO B");
        System.out.println("PAN: " + pan);
        System.out.println("SALDO INICIAL: Q" + saldoInicial);
        System.out.println("MONTO: Q" + monto);
        System.out.println("========================================");

        ISOMsg solicitud = new ISOMsg();

        solicitud.setPackager(
                isoMessageFactory.getPackager()
        );

        solicitud.setMTI("0200");

        solicitud.set(2, pan);
        solicitud.set(3, "000000");
        solicitud.set(4, "000000003000");
        solicitud.set(11, stan);
        solicitud.set(41, "ATM00002");
        solicitud.set(49, "320");

        byte[] mensajeIso =
                isoParser.pack(solicitud);

        byte[] respuestaIso =
                isoTransactionService.procesar(
                        mensajeIso
                );

        ISOMsg respuesta =
                isoParser.unpack(respuestaIso);

        System.out.println("========================================");
        System.out.println("RESPUESTA BANCO B");
        System.out.println("MTI: " + respuesta.getMTI());
        System.out.println("PAN: " + respuesta.getString(2));
        System.out.println("STAN: " + respuesta.getString(11));
        System.out.println(
                "CODIGO RESPUESTA: "
                        + respuesta.getString(39)
        );
        System.out.println("========================================");

        assertEquals(
                "0210",
                respuesta.getMTI()
        );

        assertEquals(
                pan,
                respuesta.getString(2)
        );

        assertEquals(
                stan,
                respuesta.getString(11)
        );

        assertEquals(
                "00",
                respuesta.getString(39)
        );

        BigDecimal saldoFinal =
                bancoBJdbcTemplate.queryForObject(
                        """
                        SELECT saldo
                        FROM cuentas
                        WHERE pan = ?
                        """,
                        BigDecimal.class,
                        pan
                );

        assertNotNull(saldoFinal);

        BigDecimal saldoEsperado =
                saldoInicial.subtract(monto);

        assertEquals(
                0,
                saldoEsperado.compareTo(saldoFinal)
        );

        Integer cantidadTransacciones =
                bancoBJdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM transacciones
                        WHERE cuenta_id = (
                            SELECT id
                            FROM cuentas
                            WHERE pan = ?
                        )
                        AND stan = ?
                        """,
                        Integer.class,
                        pan,
                        stan
                );

        assertNotNull(cantidadTransacciones);

        assertTrue(
                cantidadTransacciones >= 1,
                "Debe existir una transacción en Banco B"
        );

        System.out.println("========================================");
        System.out.println("BANCO B - PRUEBA EXITOSA");
        System.out.println("SALDO INICIAL: Q" + saldoInicial);
        System.out.println("PAGO: Q" + monto);
        System.out.println("SALDO FINAL: Q" + saldoFinal);
        System.out.println("TRANSACCION REGISTRADA: SI");
        System.out.println("========================================");
    }
}