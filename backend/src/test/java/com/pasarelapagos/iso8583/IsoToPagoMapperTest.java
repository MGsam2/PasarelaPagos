package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IsoToPagoMapperTest {

    private IsoToPagoMapper isoToPagoMapper;
    private IsoMessageFactory isoMessageFactory;

    @BeforeEach
    void setUp() throws Exception {
        isoMessageFactory = new IsoMessageFactory();
        isoToPagoMapper = new IsoToPagoMapper();
    }

    @Test
    void deberiaConvertirMensajeIsoAPagoRequestExitosamente() throws Exception {
        // Arrange
        String pan = "4111111111111111";
        String montoCentavos = "000000003000"; // Q 30.00
        String stan = "123456";
        String terminal = "TERM0001";
        String merchant = "MERCHANT001";

        ISOMsg isoMsg = isoMessageFactory.crearMensajePago(
                pan,
                IsoConstants.PROCESSING_CODE_PURCHASE,
                montoCentavos,
                stan,
                terminal,
                merchant
        );

        // Act
        PagoRequest request = isoToPagoMapper.convertir(isoMsg);

        // Assert
        assertNotNull(request);
        assertEquals(pan, request.pan());
        assertEquals(new BigDecimal("30.00"), request.monto());
        assertEquals(stan, request.stan());
    }

    @Test
    void deberiaLanzarExcepcionCuandoMensajeEsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> isoToPagoMapper.convertir(null)
        );

        assertEquals("El mensaje ISO 8583 no puede ser null", exception.getMessage());
    }
}