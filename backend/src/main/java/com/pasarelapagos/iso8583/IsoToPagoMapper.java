package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapeador de entrada anticorrupción para tramas ISO 8583.
 * Transforma un mensaje financiero {@link ISOMsg} (MTI 0200) al DTO inmutable {@link PagoRequest}.
 * Extrae y valida con precisión los Data Elements: DE 2, DE 3, DE 4, DE 11, DE 37 y DE 102.
 */
@Component
public class IsoToPagoMapper {

    /**
     * Convierte la trama bancaria desempaquetada a PagoRequest aplicando validaciones Fail-Fast.
     */
    public PagoRequest convertir(ISOMsg mensaje) throws Exception {
        // 1. Valida obligatoriedad y formato de la trama
        validarMensaje(mensaje);

        // 2. Extracción segura de Data Elements estándar
        String pan = mensaje.getString(2);          // DE 2 - PAN (Tarjeta)
        String processingCode = mensaje.hasField(3) // DE 3 - Processing Code
                ? mensaje.getString(3)
                : IsoConstants.PROCESSING_CODE_PURCHASE;
        String montoIso = mensaje.getString(4);     // DE 4 - Monto en centavos (12 dígitos)
        String stan = mensaje.getString(11);        // DE 11 - STAN (Auditoría)

        // DE 37 - RRN (Retrieval Reference Number)
        String rrn = mensaje.hasField(37) ? mensaje.getString(37) : null;

        // DE 102 - Cuenta Origen del tarjetahabiente
        String cuentaOrigen = mensaje.hasField(102) ? mensaje.getString(102) : null;

        // Convierte centavos implícitos ("000000010000") a BigDecimal exacto (100.00)
        BigDecimal monto = convertirMonto(montoIso);

        // Retorna DTO inmutable completo
        return new PagoRequest(pan, monto, stan, rrn, cuentaOrigen, processingCode);
    }

    /**
     * Validación de seguridad y conformidad con el estándar ISO 8583.
     */
    private void validarMensaje(ISOMsg mensaje) throws Exception {
        if (mensaje == null) {
            throw new IllegalArgumentException("El mensaje ISO 8583 no puede ser null");
        }

        // Verifica que sea solicitud de compra/autorización MTI 0200
        if (!IsoConstants.MTI_FINANCIAL_REQUEST.equals(mensaje.getMTI())) {
            throw new IllegalArgumentException(
                    "MTI no válido. Se esperaba " + IsoConstants.MTI_FINANCIAL_REQUEST
            );
        }

        // DE 2: Tarjeta obligatoria
        if (!mensaje.hasField(2)) {
            throw new IllegalArgumentException("Falta DE2 - PAN");
        }

        // DE 3: Formato de Processing Code si está presente
        if (mensaje.hasField(3)) {
            String pc = mensaje.getString(3);
            if (pc == null || pc.length() != 6) {
                throw new IllegalArgumentException("DE3 - Processing Code debe tener exactamente 6 dígitos");
            }
        }

        // DE 4: Monto obligatorio
        if (!mensaje.hasField(4)) {
            throw new IllegalArgumentException("Falta DE4 - monto");
        }

        // DE 11: STAN obligatorio
        if (!mensaje.hasField(11)) {
            throw new IllegalArgumentException("Falta DE11 - STAN");
        }
    }

    /**
     * Conversión matemática de moneda ISO 8583 (12 dígitos numéricos sin punto decimal).
     */
    private BigDecimal convertirMonto(String montoIso) {
        if (montoIso == null || montoIso.isBlank()) {
            throw new IllegalArgumentException("El monto ISO 8583 está vacío");
        }

        long centavos = Long.parseLong(montoIso);
        return BigDecimal.valueOf(centavos).movePointLeft(2);
    }
}
