package com.pasarelapagos.corebancario;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.ruteador.RuteadorPagoResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CoreBancarioWireMockTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0); // Puerto dinámico libre
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("corebancario.url", wireMockServer::baseUrl);
    }

    @Autowired
    private CoreBancarioClient coreBancarioClient;

    @Test
    void deberiaProcesarPagoCoreExitosamenteUsandoWireMock() {
        // Simular respuesta JSON exitosa del Core Bancario
        wireMockServer.stubFor(post(urlEqualTo("/api/ruteador/pago"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "banco": "BANCO_A",
                                    "codigoRespuesta": 0,
                                    "estado": "APROBADA"
                                }
                                """)
                        .withStatus(200)));

        PagoRequest request = new PagoRequest("4111111111111111", new BigDecimal("100.00"), "123456");
        RuteadorPagoResponse response = coreBancarioClient.procesarPagoCore(request);

        assertNotNull(response);
        assertEquals("BANCO_A", response.banco());
        assertEquals(0, response.codigoRespuesta());
        assertEquals("APROBADA", response.estado());
    }
}