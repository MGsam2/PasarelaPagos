package com.pasarelapagos.iso8583;

import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.repository.CuentaRepository;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IsoInactiveAccountTest {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Test
    void debeRechazarPagoPorCuentaInactiva()
            throws Exception {

        Cuenta cuenta =
                cuentaRepository.findByPan(
                        "4111111111111111"
                ).orElseThrow();

        String estadoOriginal =
                cuenta.getEstado();

        try {

            cuenta.setEstado("INACTIVA");

            cuentaRepository.save(cuenta);

            IsoMessageFactory factory =
                    new IsoMessageFactory();

            ISOMsg solicitud =
                    factory.crearMensajePago(
                            "4111111111111111",
                            IsoConstants.PROCESSING_CODE_PURCHASE,
                            "000000000500",
                            "567890",
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
                    "567890",
                    respuesta.getString(11)
            );

            assertEquals(
                    "57",
                    respuesta.getString(39)
            );

            System.out.println(
                    "========================================"
            );
            System.out.println(
                    "PRUEBA CUENTA INACTIVA"
            );
            System.out.println(
                    "========================================"
            );
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
                    "RESULTADO: CUENTA INACTIVA"
            );
            System.out.println(
                    "========================================"
            );

        } finally {

            cuenta.setEstado(estadoOriginal);

            cuentaRepository.save(cuenta);
        }
    }
}