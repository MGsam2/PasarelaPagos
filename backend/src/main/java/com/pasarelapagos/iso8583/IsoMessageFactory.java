package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Fábrica de mensajes ISO 8583.
 * Carga las especificaciones estructurales de la trama bancaria desde pasarela-packager.xml.
 * Optimizado para alta concurrencia mediante reutilización de formateadores y captura atómica de tiempo.
 */
@Component
public class IsoMessageFactory {

    private static final DateTimeFormatter FORMATO_TRANSMISION = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final DateTimeFormatter FORMATO_HORA_LOCAL = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter FORMATO_FECHA_LOCAL = DateTimeFormatter.ofPattern("MMdd");

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
     * Construye un objeto ISOMsg 0200 asignando sellos temporales atómicos y moneda estándar (DE 49).
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

        // Captura atómica única de tiempo para sincronía estricta de DE 7, DE 12 y DE 13
        LocalDateTime timestamp = LocalDateTime.now();

        // DE 7 - Fecha y hora de transmisión (MMddHHmmss)
        mensaje.set(7, timestamp.format(FORMATO_TRANSMISION));

        // DE 11 - STAN (Auditoría de 6 dígitos)
        mensaje.set(11, stan);

        // DE 12 - Hora local (HHmmss)
        mensaje.set(12, timestamp.format(FORMATO_HORA_LOCAL));

        // DE 13 - Fecha local (MMdd)
        mensaje.set(13, timestamp.format(FORMATO_FECHA_LOCAL));

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
