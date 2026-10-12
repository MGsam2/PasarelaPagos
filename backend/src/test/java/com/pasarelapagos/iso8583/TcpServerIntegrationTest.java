package com.pasarelapagos.iso8583;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TcpServerIntegrationTest {

    @Value("${tcp.server.port:9090}")
    private int tcpPort;

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoParser isoParser;

    @Test
    void deberiaProcesarTransaccionIsoViaTcpSocket() throws Exception {
        var isoMsg = isoMessageFactory.crearMensajePago(
                "4111111111111111",
                IsoConstants.PROCESSING_CODE_PURCHASE,
                "000000001000",
                "999888",
                "TERM0001",
                "MERCHANT001"
        );
        byte[] tramaEnvio = isoParser.pack(isoMsg);

        try (Socket socket = new Socket("localhost", tcpPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeShort(tramaEnvio.length);
            out.write(tramaEnvio);
            out.flush();

            int responseLength = in.readUnsignedShort();
            byte[] respuestaBytes = new byte[responseLength];
            in.readFully(respuestaBytes);

            var respuestaIso = isoParser.unpack(respuestaBytes);

            assertNotNull(respuestaIso);
            assertEquals("0210", respuestaIso.getMTI());
            assertEquals("00", respuestaIso.getString(39));
        }
    }
}