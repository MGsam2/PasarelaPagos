package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class IsoHttpClientTest {

    @Test
    void debeEnviarIso0200YRecibirIso0210() throws Exception {

        IsoMessageFactory factory =
                new IsoMessageFactory();

        ISOMsg solicitud =
                factory.crearMensajePago(
                        "4111111111111111",
                        IsoConstants.PROCESSING_CODE_PURCHASE,
                        "000000000500",
                        "345678",
                        "ATM00001",
                        "COMERCIO000001"
                );

        // Fuerza el uso de bitmap secundario.
        solicitud.set(70, "001");

        byte[] mensajeIso =
                solicitud.pack();

        HttpClient client =
                HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:8080/api/iso8583/transacciones"
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/octet-stream"
                        )
                        .header(
                                "Accept",
                                "application/octet-stream"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofByteArray(
                                        mensajeIso
                                )
                        )
                        .build();

        HttpResponse<byte[]> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        // =====================================================
        // VALIDACIONES HTTP
        // =====================================================

        assertEquals(
                200,
                response.statusCode()
        );

        assertNotNull(response.body());

        assertTrue(
                response.body().length > 0
        );

        // =====================================================
        // DESEMPAQUETAR RESPUESTA ISO 8583
        // =====================================================

        IsoParser parser =
                new IsoParser(factory);

        ISOMsg respuesta =
                parser.unpack(response.body());

        // =====================================================
        // VALIDACIONES ISO 8583
        // =====================================================

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
                "000000000500",
                respuesta.getString(4)
        );

        assertEquals(
                "345678",
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

        // =====================================================
        // INFORMACIÓN
        // =====================================================

        System.out.println("========================================");
        System.out.println("HTTP + ISO 8583 END-TO-END");
        System.out.println("========================================");
        System.out.println(
                "HTTP STATUS: " + response.statusCode()
        );
        System.out.println(
                "BYTES ENVIADOS: " + mensajeIso.length
        );
        System.out.println(
                "BYTES RECIBIDOS: " + response.body().length
        );
        System.out.println("----------------------------------------");
        System.out.println(
                "MTI RESPUESTA: " + respuesta.getMTI()
        );
        System.out.println(
                "PAN: " + respuesta.getString(2)
        );
        System.out.println(
                "STAN: " + respuesta.getString(11)
        );
        System.out.println(
                "CODIGO RESPUESTA: " + respuesta.getString(39)
        );
        System.out.println(
                "TERMINAL: " + respuesta.getString(41)
        );
        System.out.println(
                "MONEDA: " + respuesta.getString(49)
        );
        System.out.println("========================================");
        System.out.println(
                "TRANSACCION ISO 8583 COMPLETA Y APROBADA"
        );
        System.out.println("========================================");
    }
}