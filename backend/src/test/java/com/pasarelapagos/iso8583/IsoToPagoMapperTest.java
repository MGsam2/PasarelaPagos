package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.repository.CuentaRepository;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IsoToPagoMapperTest {

    @Autowired
    private IsoToPagoMapper isoToPagoMapper;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Test
    void debeConvertirIsoAPagoRequestUsandoPan() throws Exception {

        String pan = "4111111111111111";

        Cuenta cuenta = cuentaRepository.findByPan(pan)
                .orElseThrow(() ->
                        new AssertionError(
                                "No existe en BD una cuenta con el PAN del test"
                        )
                );

        ISOMsg mensaje = new ISOMsg();

        mensaje.setPackager(
                new IsoMessageFactory().getPackager()
        );

        mensaje.setMTI(
                IsoConstants.MTI_FINANCIAL_REQUEST
        );

        mensaje.set(2, pan);
        mensaje.set(3, "000000");
        mensaje.set(4, "000000003000");
        mensaje.set(7, "1005025150");
        mensaje.set(11, "123456");
        mensaje.set(12, "025150");
        mensaje.set(13, "1005");
        mensaje.set(37, "123456      ");
        mensaje.set(41, "ATM00001");
        mensaje.set(42, "COMERCIO000001");
        mensaje.set(49, "320");

        PagoRequest request =
                isoToPagoMapper.convertir(mensaje);

        assertNotNull(request);

        assertEquals(
                cuenta.getId(),
                request.getCuentaId()
        );

        assertEquals(
                new BigDecimal("30.00"),
                request.getMonto()
        );

        assertEquals(
                "123456",
                request.getStan()
        );

        System.out.println("========================================");
        System.out.println("DEMMapear ISO -> REST");
        System.out.println("========================================");
        System.out.println("DE2 PAN: " + pan);
        System.out.println("Cuenta encontrada ID: " + cuenta.getId());
        System.out.println("DE4 MONTO ISO: 000000003000");
        System.out.println("MONTO REST: Q" + request.getMonto());
        System.out.println("DE11 STAN: " + request.getStan());
        System.out.println("========================================");
        System.out.println("CONVERSION ISO -> PagoRequest EXITOSA");
    }
}