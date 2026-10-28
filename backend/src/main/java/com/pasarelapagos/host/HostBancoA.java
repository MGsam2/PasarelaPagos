package com.pasarelapagos.host;

import com.pasarelapagos.iso8583.IsoConstants;
import com.pasarelapagos.util.MaskingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Host emisor del Banco A (BIN 4111).
 * Aplica lógica de autorización bancaria, débito atómico y auditoría contable.
 * Protegido contra condiciones de carrera (Race Conditions) y con logs PCI-DSS seguros.
 */
@Service
public class HostBancoA {

    private static final Logger log = LoggerFactory.getLogger(HostBancoA.class);

    private final JdbcTemplate jdbcTemplate;

    public HostBancoA(@Qualifier("mainJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Consulta la cuenta y saldo por PAN.
     */
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

    /**
     * Motor de autorización bancaria y débito atómico transaccional.
     * Utiliza el gestor de transacciones primario (mainDataSource / pasarela_pagos).
     */
    @Transactional
    public int procesarPago(String pan, BigDecimal monto, String stan) {
        Map<String, Object> cuenta;

        // 1. Verificar existencia de la cuenta
        try {
            cuenta = buscarCuentaPorPan(pan);
        } catch (Exception e) {
            log.warn("Host Banco A: Cuenta no encontrada para PAN {}", MaskingUtils.maskPan(pan));
            return 14; // Código 14: Tarjeta no encontrada / PAN inválido
        }

        Long cuentaId = ((Number) cuenta.get("id")).longValue();

        // 2. Verificar monto válido (> 0)
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            registrarTransaccionRechazada(cuentaId, monto, stan, IsoConstants.RESPONSE_INVALID_AMOUNT);
            return 13; // Código 13: Monto inválido
        }

        // 3. Verificar estado de la cuenta
        String estado = String.valueOf(cuenta.get("estado"));
        if (!"ACTIVA".equalsIgnoreCase(estado)) {
            registrarTransaccionRechazada(cuentaId, monto, stan, IsoConstants.RESPONSE_ACCOUNT_NOT_ACTIVE);
            return 57; // Código 57: Cuenta inactiva o bloqueada
        }

        // 4. Verificar disponibilidad previa de saldo
        BigDecimal saldo = (BigDecimal) cuenta.get("saldo");
        if (saldo.compareTo(monto) < 0) {
            registrarTransaccionRechazada(cuentaId, monto, stan, IsoConstants.RESPONSE_INSUFFICIENT_FUNDS);
            return 51; // Código 51: Fondos insuficientes
        }

        // 5. Débito atómico con salvaguarda en BD (previene sobregiro en concurrencia masiva)
        String sqlUpdateSaldo = """
                UPDATE cuentas
                SET saldo = saldo - ?
                WHERE id = ? AND saldo >= ?
                """;

        int filasActualizadas = jdbcTemplate.update(sqlUpdateSaldo, monto, cuentaId, monto);
        if (filasActualizadas == 0) {
            // El saldo varió concurrentemente y ya no es suficiente
            registrarTransaccionRechazada(cuentaId, monto, stan, IsoConstants.RESPONSE_INSUFFICIENT_FUNDS);
            return 51;
        }

        // 6. Registro contable del comprobante aprobado
        registrarTransaccionAprobada(cuentaId, monto, stan);
        log.debug("Host Banco A: Pago aprobado exitosamente para PAN {}", MaskingUtils.maskPan(pan));
        return 0; // Código 00: Pago aprobado
    }

    private void registrarTransaccionAprobada(Long cuentaId, BigDecimal monto, String stan) {
        String insertTransaccion = """
                INSERT INTO transacciones
                    (cuenta_id, monto, tipo, estado, codigo_respuesta, stan, fecha_hora)
                VALUES
                    (?, ?, 'PAGO', 'APROBADA', '00', ?, CURRENT_TIMESTAMP)
                """;
        jdbcTemplate.update(insertTransaccion, cuentaId, monto, stan);
    }

    private void registrarTransaccionRechazada(Long cuentaId, BigDecimal monto, String stan, String codigoRespuesta) {
        String sql = """
                INSERT INTO transacciones
                    (cuenta_id, monto, tipo, estado, codigo_respuesta, stan, fecha_hora)
                VALUES
                    (?, ?, 'PAGO', 'RECHAZADA', ?, ?, CURRENT_TIMESTAMP)
                """;
        jdbcTemplate.update(sql, cuentaId, monto == null ? BigDecimal.ZERO : monto, codigoRespuesta, stan);
    }
}
