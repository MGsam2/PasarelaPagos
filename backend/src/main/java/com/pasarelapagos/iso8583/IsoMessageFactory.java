package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Fábrica de mensajes ISO 8583.
 * Carga las especificaciones estructurales de la trama bancaria desde pasarela-packager.xml.
 * Inicializa el packager y construye tramas financieras (MTI 0200) completas.
 */
@Component
public class IsoMessageFactory {

    private final ISOPackager packager;

    public IsoMessageFactory() throws Exception {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("iso8583/pasarela-packager.xml");

        if (inputStream == null) {
            throw new IllegalStateException(
                    "No se encontró iso8583/pasarela-packager.xml en el classpath"
            );
        }
        this.packager = new GenericPackager(inputStream);
    }

    /**
     * Construye un objeto ISOMsg 0200 asignando sellos temporales (DE 7, 12, 13) y moneda (DE 49).
     */
    public ISOMsg crearMensajePago(
            String pan,
            String processingCode,
            String monto,
            String stan,
            String terminalId,
            String merchantId) throws Exception {

        ISOMsg mensaje = new ISOMsg();
        mensaje.setPackager(packager);

        // MTI 0200 - Solicitud de autorización de compra
        mensaje.setMTI(IsoConstants.MTI_FINANCIAL_REQUEST);

        // DE 2 - Tarjeta (PAN)
        mensaje.set(2, pan);

        // DE 3 - Processing Code (000000 compra estándar)
        mensaje.set(3, processingCode);

        // DE 4 - Monto en centavos (12 dígitos numéricos)
        mensaje.set(4, monto);

        // DE 7 - Fecha y hora de transmisión (MMddHHmmss)
        mensaje.set(7, obtenerFechaTransmision());

        // DE 11 - STAN (Auditoría de 6 dígitos)
        mensaje.set(11, stan);

        // DE 12 - Hora local (HHmmss)
        mensaje.set(12, obtenerHoraLocal());

        // DE 13 - Fecha local (MMdd)
        mensaje.set(13, obtenerFechaLocal());

        // DE 37 - RRN (12 caracteres normalizados)
        mensaje.set(37, generarReferencia(stan));

        // DE 41 - Terminal POS
        mensaje.set(41, terminalId);

        // DE 42 - Identificador del Comercio
        mensaje.set(42, merchantId);

        // DE 49 - Código de Moneda ISO 4217 (320 = Quetzales GTQ)
        mensaje.set(49, IsoConstants.CURRENCY_GUATEMALA);

        return mensaje;
    }

    private String obtenerFechaTransmision() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMddHHmmss"));
    }

    private String obtenerHoraLocal() {
        return java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
    }

    private String obtenerFechaLocal() {
        return java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMdd"));
    }

    private String generarReferencia(String stan) {
        if (stan == null || stan.isBlank()) {
            return "000000000000";
        }
        String clean = stan.trim();
        if (clean.length() >= 12) {
            return clean.substring(0, 12);
        }
        try {
            long stanNum = Long.parseLong(clean);
            return String.format("%012d", stanNum);
        } catch (NumberFormatException e) {
            return String.format("%12s", clean).replace(' ', '0');
        }
    }

    public ISOPackager getPackager() {
        return packager;
    }
}
