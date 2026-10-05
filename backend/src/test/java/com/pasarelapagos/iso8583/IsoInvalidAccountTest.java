package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class IsoInvalidAccountTest {

    @Test
    void debeRechazarPanInexistente()
            throws Exception {

        IsoMessageFactory factory =
                new IsoMessageFactory();

        ISOMsg solicitud =
                factory.crearMensajePago(
                        "9999999999999999",
                        IsoConstants.PROCESSING_CODE_PURCHASE,
                        "000000000500",
                        "789012",
                        "ATM00001",
                        "COMERCIO000001"
                );

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
                                HttpRequest.BodyPublishers
                                        .ofByteArray(mensajeIso)
                        )
                        .build();

        HttpResponse<byte[]> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers
                                .ofByteArray()
                );

        assertEquals(
                200,
                response.statusCode()
        );

        IsoParser parser =
                new IsoParser(factory);

        ISOMsg respuesta =
                parser.unpack(response.body());

        assertEquals(
                "0210",
                respuesta.getMTI()
        );

        assertEquals(
                "9999999999999999",
                respuesta.getString(2)
        );

        assertEquals(
                "789012",
                respuesta.getString(11)
        );

        assertEquals(
                "14",
                respuesta.getString(39)
        );

        System.out.println(
                "========================================"
        );
        System.out.println(
                "PRUEBA PAN INEXISTENTE"
        );
        System.out.println(
                "========================================"
        );
        System.out.println(
                "HTTP STATUS: "
                        + response.statusCode()
        );
        System.out.println(
                "MTI: " + respuesta.getMTI()
        );
        System.out.println(
                "PAN: " + respuesta.getString(2)
        );
        System.out.println(
                "STAN: " + respuesta.getString(11)
        );
        System.out.println(
                "CODIGO RESPUESTA: "
                        + respuesta.getString(39)
        );
        System.out.println(
                "RESULTADO: CUENTA/PAN INVALIDO"
        );
        System.out.println(
                "========================================"
        );
    }
}