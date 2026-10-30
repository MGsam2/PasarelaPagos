package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IsoToPagoMapperTest {

    @Autowired
    private IsoMessageFactory isoMessageFactory;

    @Autowired
    private IsoToPagoMapper isoToPagoMapper;

    @Test
    @DisplayName("Criterio 2: Extrae correctamente DE 2, 3, 4, 11, 37 y 102 convirtiendo centavos a BigDecimal")
    void testExtraccionDataElementsYConversionMonto() throws Exception {
        System.out.println("\n========================================================================");
        System.out.println("🧪 PRUEBA 2: MOTOR ISO 8583 & EXTRACTION DE DATA ELEMENTS");
        System.out.println("========================================================================");

        ISOMsg msg0200 = isoMessageFactory.crearMensajePago(
                "4111111111111111", "000000", "000000010000", "123456", "TERM001", "MERCH001"
        );
        msg0200.set(37, "RRN123456789");
        msg0200.set(102, "123456789012");

        System.out.println("⚙️  [1/3] Mapped Raw Fields:");
        System.out.println("    - DE 2 (PAN): " + msg0200.getString(2));
        System.out.println("    - DE 4 (Monto en centavos): " + msg0200.getString(4));
        System.out.println("    - DE 11 (STAN): " + msg0200.getString(11));
        System.out.println("    - DE 37 (RRN): " + msg0200.getString(37));
        System.out.println("    - DE 102 (Cuenta Origen): " + msg0200.getString(102));

        PagoRequest request = isoToPagoMapper.convertir(msg0200);

        System.out.println("🔄 [2/3] Mapeo de DTO PagoRequest procesado:");
        System.out.println("    - Monto String '000000010000' -> BigDecimal: Q" + request.monto());

        System.out.println("✅ [3/3] RESULTADO: Conversión de Data Elements y formato de moneda exactos.");
        System.out.println("========================================================================\n");

        assertEquals("4111111111111111", request.pan());
        assertEquals("000000", request.processingCode());
        assertEquals(new BigDecimal("100.00"), request.monto());
        assertEquals("123456", request.stan());
        assertEquals("RRN123456789", request.rrn());
        assertEquals("123456789012", request.cuentaOrigen());
    }
}