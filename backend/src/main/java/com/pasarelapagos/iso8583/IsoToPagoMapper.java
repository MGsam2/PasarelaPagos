package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
/* MAPEADOR DE ENTRADA O DEL DESEMPAQUETADO QUE HIZO EL IsoParser
Transforma un mensaje financiero iso 8583 |ISOMsg|
hacia el objeto interno java Pagorequest */
@Component
public class IsoToPagoMapper {
//convierte la trama bancira desemapcada a PagoRequest
    public PagoRequest convertir(ISOMsg mensaje) throws Exception {
        //1 valida que el mensaje sea valido y tenga campos obligatorios
        validarMensaje(mensaje);
        //extrae  los Data Elements DE standar de ISO 8583
        String pan = mensaje.getString(2);      //DE 2 - No de tarjeta PAN
        String montoIso = mensaje.getString(4); //DE 4 - Monto en centavos sin punto
        String stan = mensaje.getString(11);    //DE 11 - Numero de Auditoria STAN
        
        // DE 37 - RRN (Retrieval Reference Number)
        String rrn = mensaje.hasField(37) ? mensaje.getString(37) : null;
        
        // DE 102 - Cuenta Origen del tarjetahabiente
        String cuentaOrigen = mensaje.hasField(102) ? mensaje.getString(102) : null;
        //convverte el monto en centavos texto de ("000000010000") a BigDecimal (100.00)
        BigDecimal monto = convertirMonto(montoIso);
        //retorna el DTO inmutable que usara la capa de servicio
        return new PagoRequest(pan, monto, stan, rrn, cuentaOrigen);
    }
/*VALIDACION DE SEGURIDAD Y PROTOCOLO
Lanza excepcion si el mensaje no cumple las reglas bancarias */
    private void validarMensaje(ISOMsg mensaje) throws Exception {

        if (mensaje == null) {
            throw new IllegalArgumentException(
                    "El mensaje ISO 8583 no puede ser null"
            );
        }
        //verifica que sea una solicitud de compra/autorizacion MTI 0200
        if (!IsoConstants.MTI_FINANCIAL_REQUEST
                .equals(mensaje.getMTI())) {

            throw new IllegalArgumentException(
                    "MTI no válido. Se esperaba "
                            + IsoConstants.MTI_FINANCIAL_REQUEST
            );
        }
        //DE 2 Tarjeta es obligatorio
        if (!mensaje.hasField(2)) {
            throw new IllegalArgumentException(
                    "Falta DE2 - PAN"
            );
        }
        //DE 4 Monto es obligatorio
        if (!mensaje.hasField(4)) {
            throw new IllegalArgumentException(
                    "Falta DE4 - monto"
            );
        }
        // DE 11 STAN es obligatorio
        if (!mensaje.hasField(11)) {
            throw new IllegalArgumentException(
                    "Falta DE11 - STAN"
            );
        }
    }
/*CONVERSION DE MONEDA FINANCIERA
    estandar ISO8583 envia monto con 12 digitos rellenos de ceros a la izquierda sin punto decimal
     000000001500 = Q15.00, mueve el punto 2 posiciones a la izquierda para obtener los centavos*/
    private BigDecimal convertirMonto(String montoIso) {

        if (montoIso == null || montoIso.isBlank()) {
            throw new IllegalArgumentException(
                    "El monto ISO 8583 está vacío"
            );
        }

        long centavos = Long.parseLong(montoIso);
        //Mueve punto decimal a la izquierda 2 posiciones 10000 -> 100.00
        return BigDecimal
                .valueOf(centavos)
                .movePointLeft(2);
    }
}




/*"El IsoToPagoMapper actúa como nuestra capa anticorrupción. 
Recibe el ISOMsg de jPOS y lo transforma en un DTO inmutable llamado PagoRequest. 
Integra validaciones Fail-Fast para abortar tramas malformadas que carezcan del MTI 0200 
o los Data Elements obligatorios. Su función más crítica es la conversión del DE 4: extrae 
la cadena ISO de 12 dígitos con decimales implícitos y utiliza BigDecimal.movePointLeft(2) 
para entregar a la capa de servicios un monto matemáticamente exacto, libre de errores de redondeo 
de punto flotante." */