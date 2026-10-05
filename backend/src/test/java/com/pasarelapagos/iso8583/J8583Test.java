package com.pasarelapagos.iso8583;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class J8583Test {

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @Test
    void debeParsearMensajeGeneradoPorJposConJ8583() throws Exception {

        // =====================================================
        // 1. jPOS CREA EL MENSAJE ISO 8583
        // =====================================================

        ISOMsg mensajeJpos = isoMessageFactory.crearMensajePago(
                "4111111111111111", // DE2 - PAN
                "000000",           // DE3 - Processing Code
                "000000003000",     // DE4 - Q30.00
                "123456",           // DE11 - STAN
                "ATM00001",         // DE41 - Terminal
                "COMERCIO000001"    // DE42 - Comercio
        );

        // Forzamos bitmap secundario para utilizar 128 bits.
        mensajeJpos.set(70, "001");

        assertEquals("0200", mensajeJpos.getMTI());

        // =====================================================
        // 2. jPOS EMPAQUETA EL MENSAJE
        // =====================================================

        byte[] mensajeEmpaquetado = isoParser.pack(mensajeJpos);

        assertNotNull(mensajeEmpaquetado);
        assertTrue(mensajeEmpaquetado.length > 0);

        System.out.println();
        System.out.println("========================================");
        System.out.println("MENSAJE GENERADO POR jPOS");
        System.out.println("========================================");

        System.out.println(
                "HEX: " + convertirHex(mensajeEmpaquetado)
        );

        System.out.println(
                "LONGITUD: " + mensajeEmpaquetado.length + " bytes"
        );

        System.out.println(
                "========================================"
        );

        // =====================================================
        // 3. j8583 CARGA SU CONFIGURACION
        // =====================================================

        MessageFactory<IsoMessage> messageFactory =
                new MessageFactory<>();

        ConfigParser.configureFromClasspathConfig(
                messageFactory,
                "iso8583/j8583.xml"
        );

        // =====================================================
        // 4. j8583 INTERPRETA EL MENSAJE GENERADO POR jPOS
        // =====================================================

        IsoMessage mensajeJ8583 =
                messageFactory.parseMessage(
                        mensajeEmpaquetado,
                        0
                );

        assertNotNull(mensajeJ8583);

        // =====================================================
        // 5. VALIDAMOS LOS CAMPOS
        // =====================================================

        assertEquals(
                0x0200,
                mensajeJ8583.getType()
        );

        assertEquals(
                "4111111111111111",
                mensajeJ8583.getField(2).toString()
        );

        assertEquals(
                "000000",
                mensajeJ8583.getField(3).toString()
        );

        assertEquals(
                "000000003000",
                mensajeJ8583.getField(4).toString()
        );

        assertEquals(
                "123456",
                mensajeJ8583.getField(11).toString()
        );

        assertEquals(
                "ATM00001",
                mensajeJ8583.getField(41).toString()
        );

        assertEquals(
                "COMERCIO000001",
                mensajeJ8583.getField(42).toString().trim()
        );

        assertEquals(
                "320",
                mensajeJ8583.getField(49).toString()
        );

        assertEquals(
                "001",
                mensajeJ8583.getField(70).toString()
        );

        // =====================================================
        // 6. CONFIRMAMOS BITMAP SECUNDARIO
        // =====================================================

        assertNotNull(
                mensajeJ8583.getField(70)
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println("MENSAJE INTERPRETADO POR j8583");
        System.out.println("========================================");

        System.out.println(
                "MTI: " +
                String.format(
                        "%04X",
                        mensajeJ8583.getType()
                )
        );

        System.out.println(
                "DE2  PAN: " +
                mensajeJ8583.getField(2)
        );

        System.out.println(
                "DE3  PROCESSING CODE: " +
                mensajeJ8583.getField(3)
        );

        System.out.println(
                "DE4  MONTO: " +
                mensajeJ8583.getField(4)
        );

        System.out.println(
                "DE11 STAN: " +
                mensajeJ8583.getField(11)
        );

        System.out.println(
                "DE41 TERMINAL: " +
                mensajeJ8583.getField(41)
        );

        System.out.println(
                "DE42 COMERCIO: " +
                mensajeJ8583.getField(42)
        );

        System.out.println(
                "DE49 MONEDA: " +
                mensajeJ8583.getField(49)
        );

        System.out.println(
                "DE70 NETWORK: " +
                mensajeJ8583.getField(70)
        );

        System.out.println(
                "========================================"
        );

        System.out.println();
        System.out.println("PRUEBA jPOS + j8583 EXITOSA");
        System.out.println("jPOS: CREA + EMPAQUETA");
        System.out.println("j8583: INTERPRETA + PARSEA");
        System.out.println("Bitmap secundario: SI");
        System.out.println();
    }

    private String convertirHex(byte[] datos) {

        StringBuilder resultado =
                new StringBuilder();

        for (byte dato : datos) {

            resultado.append(
                    String.format("%02X", dato)
            );
        }

        return resultado.toString();
    }
}