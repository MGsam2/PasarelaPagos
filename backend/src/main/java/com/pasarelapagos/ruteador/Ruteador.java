package com.pasarelapagos.ruteador;

import com.pasarelapagos.host.HostBancoA;
import com.pasarelapagos.host.HostBancoB;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;
/*CONMUTADOR FINANCIERO -SWITCH- 
lee los primero digitos de la tarjeta BIN y redirige la solicitud
hacie el host del banco autorizador correspondiente Banco A o B*/
@Component
public class Ruteador {

    private final HostBancoA hostBancoA;
    private final HostBancoB hostBancoB;

    public Ruteador(
            HostBancoA hostBancoA,
            HostBancoB hostBancoB) {

        this.hostBancoA = hostBancoA;
        this.hostBancoB = hostBancoB;
    }
/* EVALUACION DE BIN Bank Identification Number 
Limpia los espacion del PAN y determina que banco debe procesar la transaccion*/
    public String determinarBanco(String pan) {

        if (pan == null || pan.isBlank()) {
            return "DESCONOCIDO";
        }
        //elimina espacio en blanco que puedan venir en el string 4111 1111 -> 41111111
        String panLimpio = pan.replaceAll("\\s+", "");
        
        //BIN 4111: Enruta a Banco A
        if (panLimpio.startsWith("4111")) {
            return "BANCO_A";
        }

        //BIN 5222: Enruta a Banco B
        if (panLimpio.startsWith("5222")) {
            return "BANCO_B";
        }

        return "DESCONOCIDO";
    }
    /*CONSULTA DE SALDO/CUENTA
        Redirige la consulta de informacion de la cuenta al host indicado por el bin */
    public Map<String, Object> buscarCuentaPorPan(String pan) {

        String banco = determinarBanco(pan);

        String panLimpio =
                pan.replaceAll("\\s+", "");

        //Uso de switch expresiones de Java moderno
        return switch (banco) {

            case "BANCO_A" ->
                    hostBancoA.buscarCuentaPorPan(
                            panLimpio
                    );

            case "BANCO_B" ->
                    hostBancoB.buscarCuentaPorPan(
                            panLimpio
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Banco no soportado para el PAN: "
                                    + pan
                    );
        };
    }

    /*PROCESAMIENTO DE DEBITO /PAGO
        canaliza la cobranza al host del banco emisor y retorna el codigo de respuesta 00, 51, 14 */
    public int procesarPago(
            String pan,
            BigDecimal monto,
            String stan) {

        String banco = determinarBanco(pan);

        String panLimpio =
                pan.replaceAll("\\s+", "");

        return switch (banco) {

            case "BANCO_A" ->
                    hostBancoA.procesarPago(
                            panLimpio,
                            monto,
                            stan
                    );

            case "BANCO_B" ->
                    hostBancoB.procesarPago(
                            panLimpio,
                            monto,
                            stan
                    );
            //Si el BIN no coincide retorna directamente el codigo 14(tarjeta invalida)
            default ->
                    14;
        };
    }
}

/*"El Ruteador es el switch financiero del sistema. 
Se encarga de evaluar el BIN (los primeros dígitos del PAN) para conmutar la transacción. 
Si la tarjeta empieza con 4111, canaliza el débito hacia el HostBancoA, si empieza con 5222, 
lo envía al HostBancoB, y si el BIN no es reconocido, retorna inmediatamente el código de rechazo 14."

"El Ruteador funciona como el 'Switch' o conmutador financiero de la arquitectura. 
Su principal responsabilidad es la evaluación del BIN (Bank Identification Number) extraído del PAN. 
Para garantizar un enrutamiento de latencia ultra baja, ejecuta reglas de prefijo 
en memoria (como 4111 para Banco A y 5222 para Banco B) en lugar de realizar consultas externas. 
Al emplear expresiones 'switch' modernas de Java y técnicas de sanitización de cadenas, canaliza 
de forma segura la ejecución del débito hacia el Host correspondiente, o aborta el flujo con un 
código 14 si la institución emisora no es soportada."*/