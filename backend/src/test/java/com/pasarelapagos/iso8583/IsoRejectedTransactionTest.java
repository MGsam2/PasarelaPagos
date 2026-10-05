package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class IsoRejectedTransactionTest {

    @Test
    void debeRechazarPagoPorFondosInsuficientes()
            throws Exception {

        IsoMessageFactory factory =
                new IsoMessageFactory();

        ISOMsg solicitud =
                factory.crearMensajePago(
                        "4111111111111111",
                        IsoConstants.PROCESSING_CODE_PURCHASE,
                        "000000005000",
                        "456789",
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
                "456789",
                respuesta.getString(11)
        );

        assertEquals(
                "51",
                respuesta.getString(39)
        );

        System.out.println("========================================");
        System.out.println("PRUEBA FONDOS INSUFICIENTES");
        System.out.println("========================================");
        System.out.println(
                "MTI: " + respuesta.getMTI()
        );
        System.out.println(
                "STAN: " + respuesta.getString(11)
        );
        System.out.println(
                "CODIGO RESPUESTA: "
                        + respuesta.getString(39)
        );
        System.out.println(
                "RESULTADO: FONDOS INSUFICIENTES"
        );
        System.out.println("========================================");
    }
}