package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = { "tcp.server.port=9091" }
)
class TcpServerIntegrationTest {

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @Test
    @DisplayName("Criterio 1: Debe enviar trama ISO 0200 por el Socket TCP y recibir respuesta MTI 0210")
    void testComunicacionSocketTcp() throws Exception {
        System.out.println("\n========================================================================");
        System.out.println("🧪 PRUEBA 1: INGESTA TCP & SERVIDOR DE SOCKETS (PUERTO 9091)");
        System.out.println("========================================================================");

        // Cronómetro de precisión para medir el tiempo de socket end-to-end
        long startTime = System.nanoTime();

        try (Socket socket = new Socket("localhost", 9091);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            System.out.println("🔌 [1/6] Conexión establecida exitosamente con el Socket TCP en puerto 9091");

            // 1. Construir MTI 0200
            byte[] payload = isoParser.pack(
                isoMessageFactory.crearMensajePago("4111111111111111", "000000", "000000010000", "123456", "TERM001", "MERCH001")
            );

            // 2. Adjuntar cabecera de 2 bytes
            byte[] request = new byte[2 + payload.length];
            request[0] = (byte) ((payload.length >> 8) & 0xFF);
            request[1] = (byte) (payload.length & 0xFF);
            System.arraycopy(payload, 0, request, 2, payload.length);

            System.out.println("📤 [2/6] Enviando trama ISO 8583 (MTI 0200) de " + payload.length + " bytes con cabecera de longitud...");
            out.write(request);
            out.flush();

            // 3. Leer longitud de respuesta
            byte[] lenBuf = new byte[2];
            int readLen = in.read(lenBuf);
            assertEquals(2, readLen);
            int responseLen = ((lenBuf[0] & 0xFF) << 8) | (lenBuf[1] & 0xFF);

            System.out.println("📥 [3/6] Cabecera de respuesta recibida. Tamaño esperado de trama: " + responseLen + " bytes");

            // 4. Leer cuerpo
            byte[] responseBody = new byte[responseLen];
            int totalBytesRead = 0;
            while (totalBytesRead < responseLen) {
                int bytesRead = in.read(responseBody, totalBytesRead, responseLen - totalBytesRead);
                if (bytesRead == -1) break;
                totalBytesRead += bytesRead;
            }

            ISOMsg respuesta = isoParser.unpack(responseBody);
            
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;

            System.out.println("📦 [4/6] Trama MTI " + respuesta.getMTI() + " desempaquetada correctamente desde el Socket");
            System.out.printf("⏱️  [5/6] Latencia de Red y Sockets (End-to-End): %.3f ms\n", durationMs);
            System.out.println("✅ [6/6] RESULTADO: Respuesta de socket TCP procesada exitosamente.");
            System.out.println("========================================================================\n");

            assertEquals(responseLen, totalBytesRead);
            assertNotNull(respuesta);
            assertEquals("0210", respuesta.getMTI());
            assertTrue(durationMs < 3000, "La comunicación TCP debe completarse en menos de 3 segundos");
        }
    }
}