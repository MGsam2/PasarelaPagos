package com.pasarelapagos.corebancario;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pasarelapagos.iso8583.IsoMessageFactory;
import com.pasarelapagos.iso8583.IsoParser;
import com.pasarelapagos.iso8583.IsoTransactionService;
import com.solab.iso8583.IsoMessage;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CoreBancarioWireMockTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private IsoTransactionService isoTransactionService;

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("corebancario.url", () -> "http://localhost:8089");
        registry.add("corebancario.adapter.mode", () -> "http");
    }

    @Test
    @DisplayName("Criterio 3: Procesa pago exitoso con RestClient y retorna MTI 0210 con DE 39 = '00'")
    void testProcesarPagoExitosoRest() throws Exception {
        System.out.println("\n========================================================================");
        System.out.println("🧪 PRUEBA 3A: INTEGRACIÓN RESTCLIENT CON CORE BANCARIO (EXITOSA)");
        System.out.println("========================================================================");

        wireMockServer.stubFor(post(urlEqualTo("/api/ruteador/pago"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"banco\":\"BANCO_A\",\"codigoRespuesta\":0,\"estado\":\"APROBADA\"}")
                        .withStatus(200)));

        System.out.println("🌐 [1/3] WireMock configurado respondiendo HTTP 200 OK a /api/ruteador/pago");

        byte[] requestBytes = isoParser.pack(
                isoMessageFactory.crearMensajePago("4111111111111111", "000000", "000000010000", "123456", "TERM001", "MERCH001")
        );

        byte[] responseBytes = isoTransactionService.procesar(requestBytes);
        ISOMsg respuesta = isoParser.unpack(responseBytes);

        System.out.println("📥 [2/3] Respuesta del Core Bancario recibida vía RestClient");
        System.out.println("🎯 [3/3] MTI: " + respuesta.getMTI() + " | DE 39 (Código Respuesta): " + respuesta.getString(39) + " (APROBADO)");
        System.out.println("========================================================================\n");

        assertEquals("0210", respuesta.getMTI());
        assertEquals("00", respuesta.getString(39));
    }

    @Test
    @DisplayName("Criterio 3: Ante caída HTTP 500 del Core Bancario debe retornar MTI 0210 con DE 39 = '96' (System Error)")
    void testFallbackResilienciaIso96() throws Exception {
        System.out.println("\n========================================================================");
        System.out.println("🧪 PRUEBA 3B: RESILIENCIA HTTP & FALLBACK A CÓDIGO ISO 96 (SYSTEM ERROR)");
        System.out.println("========================================================================");

        wireMockServer.stubFor(post(urlEqualTo("/api/ruteador/pago"))
                .willReturn(aResponse().withStatus(500)));

        System.out.println("⚠️  [1/3] Simulando caída del Core Bancario con WireMock respondiendo HTTP 500 Internal Server Error");

        byte[] requestBytes = isoParser.pack(
                isoMessageFactory.crearMensajePago("4111111111111111", "000000", "000000010000", "654321", "TERM001", "MERCH001")
        );

        System.out.println("🛡️  [2/3] Ejecutando petición a través de CoreBancarioClient...");
        byte[] responseBytes = isoTransactionService.procesar(requestBytes);
        ISOMsg respuesta = isoParser.unpack(responseBytes);

        System.out.println("🚨 [3/3] Excepción capturada en bloque catch -> MTI: " + respuesta.getMTI() + " | DE 39: " + respuesta.getString(39) + " (System Error)");
        System.out.println("✅ RESULTADO: Degradación elegante completada sin tumbar el hilo de ejecución.");
        System.out.println("========================================================================\n");

        assertEquals("0210", respuesta.getMTI());
        assertEquals("96", respuesta.getString(39));
    }

@Test
    @DisplayName("Criterio 3C: Declinación por Fondos Insuficientes (ISO 51)")
    void debeRetornarCodigo51_CuandoCoreRechazaPorFondosInsuficientes() throws Exception {
        System.out.println("========================================================================");
        System.out.println("🚀 PRUEBA 3C: DECLINACIÓN POR FONDOS INSUFICIENTES (ISO 51)");
        System.out.println("========================================================================");
        
        // 1. Simulamos que el Core Bancario responde un error de negocio (HTTP 400 o 200 con código interno)
        wireMockServer.stubFor(post(urlEqualTo("/api/ruteador/pago"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"estado\": \"RECHAZADO\", \"motivo\": \"FONDOS_INSUFICIENTES\"}")));

        // 2. Creamos y empaquetamos la trama ISO de solicitud (Cambiamos el STAN para evitar duplicados)
        byte[] requestBytes = isoParser.pack(
                isoMessageFactory.crearMensajePago("4111111111111111", "000000", "000000010000", "111222", "TERM001", "MERCH001")
        );

        // 3. Ejecutamos la petición enviando la trama ISO
        byte[] responseBytes = isoTransactionService.procesar(requestBytes);

        // 4. Desempaquetamos la respuesta y validamos que el DE 39 sea "51"
        ISOMsg respuesta = isoParser.unpack(responseBytes);
        
        assertNotNull(respuesta);
        assertEquals("0210", respuesta.getMTI());
        assertEquals("51", respuesta.getString(39));
        
        System.out.println("✅ RESULTADO: El sistema mapeó correctamente el rechazo a DE 39: 51");
        System.out.println("========================================================================\n");
    }

    @Test
    @DisplayName("Criterio 3D: Declinación por Tarjeta Vencida (ISO 54)")
    void debeRetornarCodigo54_CuandoCoreRechazaPorTarjetaVencida() throws Exception {
        System.out.println("========================================================================");
        System.out.println("🚀 PRUEBA 3D: DECLINACIÓN POR TARJETA VENCIDA (ISO 54)");
        System.out.println("========================================================================");
        
        // 1. Simulamos que el Core Bancario rechaza la tarjeta
        wireMockServer.stubFor(post(urlEqualTo("/api/ruteador/pago"))
                .willReturn(aResponse()
                        .withStatus(400) 
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"estado\": \"RECHAZADO\", \"motivo\": \"TARJETA_VENCIDA\"}")));

        // 2. Creamos y empaquetamos la trama ISO (Cambiamos el STAN para evitar duplicados)
        byte[] requestBytes = isoParser.pack(
                isoMessageFactory.crearMensajePago("4111111111111111", "000000", "000000010000", "333444", "TERM001", "MERCH001")
        );

        // 3. Ejecutamos la petición
        byte[] responseBytes = isoTransactionService.procesar(requestBytes);

        // 4. Desempaquetamos y validamos el DE 39 = "54"
        ISOMsg respuesta = isoParser.unpack(responseBytes);
        
        assertNotNull(respuesta);
        assertEquals("0210", respuesta.getMTI());
        assertEquals("54", respuesta.getString(39));
        
        System.out.println("✅ RESULTADO: El sistema mapeó correctamente el rechazo a DE 39: 54");
        System.out.println("========================================================================\n");
    }
}