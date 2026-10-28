package com.pasarelapagos.util;

/**
 * Utilidades de formato y seguridad para el protocolo ISO 8583.
 * Implementa enmascaramiento seguro conforme al estándar PCI-DSS.
 */
public final class MaskingUtils {

    private MaskingUtils() {
    }

    /**
     * Enmascara un Primary Account Number (PAN) protegiendo datos sensibles.
     * Muestra únicamente los primeros 6 dígitos (BIN) y los últimos 4 dígitos.
     * Ejemplo: "4111 1111 1111 1111" -> "411111******1111"
     */
    public static String maskPan(String pan) {
        if (pan == null || pan.isBlank()) {
            return "****";
        }
        String cleanPan = pan.replaceAll("\\s+", "");
        int length = cleanPan.length();
        if (length <= 10) {
            return "*".repeat(length);
        }
        String bin = cleanPan.substring(0, 6);
        String lastFour = cleanPan.substring(length - 4);
        int maskedCount = length - 10;
        return bin + "*".repeat(maskedCount) + lastFour;
    }
}

