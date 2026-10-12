package com.pasarelapagos.ruteador;

import com.pasarelapagos.host.HostBancoA;
import com.pasarelapagos.host.HostBancoB;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

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

    public String determinarBanco(String pan) {

        if (pan == null || pan.isBlank()) {
            return "DESCONOCIDO";
        }

        String panLimpio = pan.replaceAll("\\s+", "");

        if (panLimpio.startsWith("4111")) {
            return "BANCO_A";
        }

        if (panLimpio.startsWith("5222")) {
            return "BANCO_B";
        }

        return "DESCONOCIDO";
    }

    public Map<String, Object> buscarCuentaPorPan(String pan) {

        String banco = determinarBanco(pan);

        String panLimpio =
                pan.replaceAll("\\s+", "");

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

            default ->
                    14;
        };
    }
}