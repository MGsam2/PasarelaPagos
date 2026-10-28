package com.pasarelapagos.ruteador;

import com.pasarelapagos.host.HostBancoA;
import com.pasarelapagos.host.HostBancoB;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Switch conmutador financiero.
 * Evalúa el BIN (Bank Identification Number) y enruta la transacción hacia el Host autorizador correspondiente.
 * Optimizado para latencia ultra-baja sin asignaciones innecesarias de memoria.
 */
@Component
public class Ruteador {

    private final HostBancoA hostBancoA;
    private final HostBancoB hostBancoB;

    public Ruteador(HostBancoA hostBancoA, HostBancoB hostBancoB) {
        this.hostBancoA = hostBancoA;
        this.hostBancoB = hostBancoB;
    }

    /**
     * Sanitiza el número de tarjeta eliminando espacios en blanco de forma eficiente.
     */
    public String limpiarPan(String pan) {
        if (pan == null) {
            return "";
        }
        return pan.replace(" ", "");
    }

    /**
     * Determina el banco emisor a partir del prefijo BIN del PAN.
     */
    public String determinarBanco(String pan) {
        String panLimpio = limpiarPan(pan);
        if (panLimpio.isBlank()) {
            return "DESCONOCIDO";
        }
        if (panLimpio.startsWith("4111")) {
            return "BANCO_A";
        }
        if (panLimpio.startsWith("5222")) {
            return "BANCO_B";
        }
        return "DESCONOCIDO";
    }

    /**
     * Consulta de cuenta bancaria delegando en el Host correspondiente.
     */
    public Map<String, Object> buscarCuentaPorPan(String pan) {
        String panLimpio = limpiarPan(pan);
        String banco = determinarBanco(panLimpio);

        return switch (banco) {
            case "BANCO_A" -> hostBancoA.buscarCuentaPorPan(panLimpio);
            case "BANCO_B" -> hostBancoB.buscarCuentaPorPan(panLimpio);
            default -> throw new IllegalArgumentException("Banco no soportado para el PAN: " + pan);
        };
    }

    /**
     * Conmuta y procesa el débito hacia el host emisor del banco correspondiente.
     */
    public int procesarPago(String pan, BigDecimal monto, String stan) {
        String panLimpio = limpiarPan(pan);
        String banco = determinarBanco(panLimpio);

        return switch (banco) {
            case "BANCO_A" -> hostBancoA.procesarPago(panLimpio, monto, stan);
            case "BANCO_B" -> hostBancoB.procesarPago(panLimpio, monto, stan);
            default -> 14; // Código 14: Institución o tarjeta no encontrada
        };
    }
}
