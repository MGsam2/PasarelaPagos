package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.stereotype.Component;

import java.io.InputStream;
/*CREA MENSAJES Y PLANTILLAD SEL ISO 8583
carga reglas estructurales de la trama bancaria en un XML Pasarela-packager.xml
Si el IsoParser abre y los mappers llenan los formularios, 
IsoMessageFactory es la imprenta y la fábrica de moldes:*/
@Component
public class IsoMessageFactory {

    private final ISOPackager packager;
        /*CONSTRUCTOR ejecuta al alevantar springboot
        lee el archivo XML que contiene las especificaciones de cada DE */
    public IsoMessageFactory() throws Exception {

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("iso8583/pasarela-packager.xml");

        if (inputStream == null) {
            throw new IllegalStateException(
                    "No se encontró iso8583/pasarela-packager.xml en el classpath"
            );
        }
        //interpreta las reglas descritas en el XML
        this.packager = new GenericPackager(inputStream);
    }
/* CREA SOLICITUDES 
construye un objeto isomsg 0200 asignado automaticamente 
los sellos de tiempo DE 7, DE 12, DE 13 y la moneda de GT DE 49*/
    public ISOMsg crearMensajePago(
            String pan,
            String processingCode,
            String monto,
            String stan,
            String terminalId,
            String merchantId) throws Exception {

        ISOMsg mensaje = new ISOMsg();
                // le asignamos el packager/plantilla XML
        mensaje.setPackager(packager);

        // MTI 0200 solicitud compra/ autorizacion
        mensaje.setMTI(IsoConstants.MTI_FINANCIAL_REQUEST);

        // DE2 - tarjeta PAN
        mensaje.set(2, pan);

        // DE3 - Processing Code 000000
        mensaje.set(3, processingCode);

        // DE4 - Amount monto en centavos
        mensaje.set(4, monto);

        // DE7 - Transmission Date/Time fecha y hora de transmision MMddHHmmss
        mensaje.set(7, obtenerFechaTransmision());

        // DE11 - STAN, numero de auditoria
        mensaje.set(11, stan);

        // DE12 - Local Time HOra local
        mensaje.set(12, obtenerHoraLocal());

        // DE13 - Local Date Fecha local
        mensaje.set(13, obtenerFechaLocal());

        // DE37 - Retrieval Reference RRN asignado a 12 caracteres
        mensaje.set(37, generarReferencia(stan));

        // DE41 - Terminal  POS
        mensaje.set(41, terminalId);

        // DE42 - Merchant ID, ID del comercio
        mensaje.set(42, merchantId);

        // DE49 - Currency Code, MOneda 320 = Quetzales
        mensaje.set(49, IsoConstants.CURRENCY_GUATEMALA);

        return mensaje;
    }
    //METODO DE FECHA Y HORA AUTOMATICOS
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
//Devuelve el packager configurado para ser usado por el IsoParser O Mappers
    public ISOPackager getPackager() {
        return packager;
    }
}
//"El IsoMessageFactory es el componente responsable de inicializar el GenericPackager de jPOS leyendo el mapa de la trama desde pasarela-packager.xml. 
// Además, nos proporciona métodos utilitarios para construir mensajes ISO 0200 válidos asignando automáticamente los campos temporales (DE 7, 12 y 13) 
// y la moneda nacional (DE 49)."