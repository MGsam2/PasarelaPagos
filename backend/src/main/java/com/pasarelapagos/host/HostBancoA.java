package com.pasarelapagos.host;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class HostBancoA {

    private final JdbcTemplate jdbcTemplate;

    public HostBancoA(
            @Qualifier("mainJdbcTemplate")
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;

        System.out.println("========================================");
        System.out.println("HOST BANCO A INICIALIZADO");
        System.out.println("JdbcTemplate: MAIN");
        System.out.println("========================================");
    }

    public Map<String, Object> buscarCuentaPorPan(String pan) {

        String sql = """
                SELECT id,
                       numero_cuenta,
                       pan,
                       saldo,
                       estado
                FROM cuentas
                WHERE pan = ?
                """;

        return jdbcTemplate.queryForMap(sql, pan);
    }

    public int procesarPago(
            String pan,
            BigDecimal monto,
            String stan) {

        Map<String, Object> cuenta;

        try {

            cuenta = buscarCuentaPorPan(pan);

        } catch (Exception e) {

            System.err.println("========================================");
            System.err.println("ERROR EN HOST BANCO A");
            System.err.println("PAN: " + pan);
            System.err.println("MENSAJE: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");

            return 14;
        }

        // Código 13 - monto inválido
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

        // Código 57 - cuenta inactiva
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

        // Código 51 - fondos insuficientes
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

        // Código 00 - pago aprobado
        registrarTransaccion(
                pan,
                monto,
                stan,
                "00",
                "APROBADA"
        );

        return 0;
    }

    private void registrarTransaccion(
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

        jdbcTemplate.update(
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

        jdbcTemplate.update(
                insertTransaccion,
                cuentaId,
                monto,
                "PAGO",
                estadoTransaccion,
                codigoRespuesta,
                stan
        );
    }

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

        jdbcTemplate.update(
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