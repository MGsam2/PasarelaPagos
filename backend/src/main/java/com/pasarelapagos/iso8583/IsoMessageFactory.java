package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.stereotype.Component;

import java.io.InputStream;

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

    public ISOMsg crearMensajePago(
            String pan,
            String processingCode,
            String monto,
            String stan,
            String terminalId,
            String merchantId) throws Exception {

        ISOMsg mensaje = new ISOMsg();

        mensaje.setPackager(packager);

        // MTI
        mensaje.setMTI(IsoConstants.MTI_FINANCIAL_REQUEST);

        // DE2 - PAN
        mensaje.set(2, pan);

        // DE3 - Processing Code
        mensaje.set(3, processingCode);

        // DE4 - Amount
        mensaje.set(4, monto);

        // DE7 - Transmission Date/Time
        mensaje.set(7, obtenerFechaTransmision());

        // DE11 - STAN
        mensaje.set(11, stan);

        // DE12 - Local Time
        mensaje.set(12, obtenerHoraLocal());

        // DE13 - Local Date
        mensaje.set(13, obtenerFechaLocal());

        // DE37 - Retrieval Reference
        mensaje.set(37, generarReferencia(stan));

        // DE41 - Terminal ID
        mensaje.set(41, terminalId);

        // DE42 - Merchant ID
        mensaje.set(42, merchantId);

        // DE49 - Currency Code
        mensaje.set(49, IsoConstants.CURRENCY_GUATEMALA);

        return mensaje;
    }

    private String obtenerFechaTransmision() {
        return java.time.LocalDateTime.now()
                .format(
                        java.time.format.DateTimeFormatter
                                .ofPattern("MMddHHmmss")
                );
    }

    private String obtenerHoraLocal() {
        return java.time.LocalTime.now()
                .format(
                        java.time.format.DateTimeFormatter
                                .ofPattern("HHmmss")
                );
    }

    private String obtenerFechaLocal() {
        return java.time.LocalDate.now()
                .format(
                        java.time.format.DateTimeFormatter
                                .ofPattern("MMdd")
                );
    }

    private String generarReferencia(String stan) {
        return String.format("%-12s", stan)
                .substring(0, 12);
    }

    public ISOPackager getPackager() {
        return packager;
    }
}