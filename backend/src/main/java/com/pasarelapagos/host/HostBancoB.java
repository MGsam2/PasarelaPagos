package com.pasarelapagos.host;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class HostBancoB {

    private final JdbcTemplate bancoBJdbcTemplate;

    public HostBancoB(
            @Qualifier("bancoBJdbcTemplate")
            JdbcTemplate bancoBJdbcTemplate) {

        this.bancoBJdbcTemplate = bancoBJdbcTemplate;

        System.out.println("========================================");
        System.out.println("HOST BANCO B INICIALIZADO");
        System.out.println("JdbcTemplate: BANCO B");
        System.out.println("========================================");
    }

    /**
     * Busca una cuenta del Banco B utilizando el PAN.
     */
    public Map<String, Object> buscarCuentaPorPan(String pan) {

        String sql = """
                SELECT
                    id,
                    cliente_id,
                    numero_cuenta,
                    pan,
                    saldo,
                    estado
                FROM cuentas
                WHERE pan = ?
                """;

        return bancoBJdbcTemplate.queryForMap(sql, pan);
    }

    /**
     * Valida si la cuenta del Banco B puede realizar el pago.
     */
    public int validarPago(
            String pan,
            BigDecimal monto) {

        Map<String, Object> cuenta =
                buscarCuentaPorPan(pan);

        String estado =
                String.valueOf(cuenta.get("estado"));

        if (!"ACTIVA".equalsIgnoreCase(estado)) {
            return 57;
        }

        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {
            return 13;
        }

        BigDecimal saldo =
                (BigDecimal) cuenta.get("saldo");

        if (saldo.compareTo(monto) < 0) {
            return 51;
        }

        return 0;
    }

    /**
     * Registra la transacción y descuenta el saldo
     * después de que las validaciones fueron exitosas.
     */
    public void registrarTransaccion(
            String pan,
            BigDecimal monto,
            String stan,
            String codigoRespuesta,
            String estadoTransaccion) {

        Map<String, Object> cuenta =
                buscarCuentaPorPan(pan);

        Long cuentaId =
                ((Number) cuenta.get("id")).longValue();

        String updateSaldo = """
                UPDATE cuentas
                SET saldo = saldo - ?
                WHERE id = ?
                """;

        bancoBJdbcTemplate.update(
                updateSaldo,
                monto,
                cuentaId
        );

        String insertTransaccion = """
                INSERT INTO transacciones
                    (cuenta_id,
                     monto,
                     tipo,
                     estado,
                     codigo_respuesta,
                     stan,
                     fecha_hora)
                VALUES
                    (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        bancoBJdbcTemplate.update(
                insertTransaccion,
                cuentaId,
                monto,
                "PAGO",
                estadoTransaccion,
                codigoRespuesta,
                stan
        );
    }

    /**
     * Ejecuta el flujo completo de pago para Banco B.
     */
    public int procesarPago(
            String pan,
            BigDecimal monto,
            String stan) {

        Map<String, Object> cuenta;

        // Validar existencia de la cuenta
        try {

            cuenta = buscarCuentaPorPan(pan);

        } catch (Exception e) {

            System.err.println("========================================");
            System.err.println("ERROR EN HOST BANCO B");
            System.err.println("PAN: " + pan);
            System.err.println("MENSAJE: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");

            return 14;
        }

        // Validar monto
        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {

            registrarTransaccionRechazada(
                    cuenta,
                    monto,
                    stan,
                    "13"
            );

            return 13;
        }

        // Validar estado de la cuenta
        String estado =
                String.valueOf(cuenta.get("estado"));

        if (!"ACTIVA".equalsIgnoreCase(estado)) {

            registrarTransaccionRechazada(
                    cuenta,
                    monto,
                    stan,
                    "57"
            );

            return 57;
        }

        // Validar saldo
        BigDecimal saldo =
                (BigDecimal) cuenta.get("saldo");

        if (saldo.compareTo(monto) < 0) {

            registrarTransaccionRechazada(
                    cuenta,
                    monto,
                    stan,
                    "51"
            );

            return 51;
        }

        // Pago aprobado
        registrarTransaccion(
                pan,
                monto,
                stan,
                "00",
                "APROBADA"
        );

        return 0;
    }

    /**
     * Registra una transacción rechazada sin descontar saldo.
     */
    private void registrarTransaccionRechazada(
            Map<String, Object> cuenta,
            BigDecimal monto,
            String stan,
            String codigoRespuesta) {

        Long cuentaId =
                ((Number) cuenta.get("id")).longValue();

        String sql = """
                INSERT INTO transacciones
                    (cuenta_id,
                     monto,
                     tipo,
                     estado,
                     codigo_respuesta,
                     stan,
                     fecha_hora)
                VALUES
                    (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        bancoBJdbcTemplate.update(
                sql,
                cuentaId,
                monto == null
                        ? BigDecimal.ZERO
                        : monto,
                "PAGO",
                "RECHAZADA",
                codigoRespuesta,
                stan
        );
    }
}