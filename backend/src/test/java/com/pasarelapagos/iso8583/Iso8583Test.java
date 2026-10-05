package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Iso8583Test {

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @Test
    void debeCrearEmpaquetarYDesempaquetarMensaje0200() throws Exception {

        // Crear mensaje ISO 8583
        ISOMsg mensaje = isoMessageFactory.crearMensajePago(
                "4111111111111111", // DE2
                "000000",           // DE3
                "000000003000",     // DE4 = Q30.00
                "123456",           // DE11
                "ATM00001",         // DE41
                "COMERCIO000001"    // DE42
        );

        // Agregamos DE70 para forzar el bitmap secundario.
        // DE70 está entre los campos 65-128.
        mensaje.set(70, "001");

        // Verificar MTI
        assertEquals("0200", mensaje.getMTI());

        // Verificar campos principales
        assertEquals("4111111111111111", mensaje.getString(2));
        assertEquals("000000", mensaje.getString(3));
        assertEquals("000000003000", mensaje.getString(4));
        assertEquals("123456", mensaje.getString(11));
        assertEquals("ATM00001", mensaje.getString(41));
        assertEquals("COMERCIO000001", mensaje.getString(42));
        assertEquals("320", mensaje.getString(49));

        // Verificar DE70
        assertEquals("001", mensaje.getString(70));

        // Empaquetar
        byte[] datos = isoParser.pack(mensaje);

        assertNotNull(datos);
        assertTrue(datos.length > 0);

        // Mostrar mensaje empaquetado
        System.out.println();
        System.out.println("========================================");
        System.out.println("MENSAJE ISO 8583 EMPAQUETADO");
        System.out.println("========================================");

        System.out.println(
                "HEX: " + convertirHex(datos)
        );

        System.out.println(
                "LONGITUD: " + datos.length + " bytes"
        );

        System.out.println(
                "========================================"
        );

        // Desempaquetar
        ISOMsg mensajeRecibido = isoParser.unpack(datos);

        // Verificar que recuperamos correctamente los datos
        assertEquals("0200", mensajeRecibido.getMTI());

        assertEquals(
                "4111111111111111",
                mensajeRecibido.getString(2)
        );

        assertEquals(
                "000000",
                mensajeRecibido.getString(3)
        );

        assertEquals(
                "000000003000",
                mensajeRecibido.getString(4)
        );

        assertEquals(
                "123456",
                mensajeRecibido.getString(11)
        );

        assertEquals(
                "001",
                mensajeRecibido.getString(70)
        );

        // Verificar que DE70 realmente está presente
        assertTrue(mensajeRecibido.hasField(70));

        System.out.println();
        System.out.println("========================================");
        System.out.println("MENSAJE ISO 8583 DESEMPAQUETADO");
        System.out.println("========================================");

        System.out.println(
                mensajeRecibido.toString()
        );

        System.out.println(
                "========================================"
        );

        System.out.println();
        System.out.println("PRUEBA ISO 8583 EXITOSA");
        System.out.println("Bitmap secundario utilizado: SI");
        System.out.println();
    }

    private String convertirHex(byte[] datos) {

        StringBuilder resultado = new StringBuilder();

        for (byte dato : datos) {
            resultado.append(
                    String.format("%02X", dato)
            );
        }

        return resultado.toString();
    }
}