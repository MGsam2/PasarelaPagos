package com.pasarelapagos.host;

import com.pasarelapagos.iso8583.IsoConstants;
import com.pasarelapagos.util.MaskingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Host emisor del Banco B (BIN 5222).
 * Conecta con el pool de conexiones dedicado a la base de datos de Banco B.
 * Optimizado para alta concurrencia: consultas en un solo paso y débito atómico.
 */
@Component
public class HostBancoB {

    private static final Logger log = LoggerFactory.getLogger(HostBancoB.class);

    private final JdbcTemplate bancoBJdbcTemplate;

    public HostBancoB(@Qualifier("bancoBJdbcTemplate") JdbcTemplate bancoBJdbcTemplate) {
        this.bancoBJdbcTemplate = bancoBJdbcTemplate;
    }

    /**
     * Consulta la cuenta bancaria por PAN en la BD de Banco B.
     */
    public Map<String, Object> buscarCuentaPorPan(String pan) {
        String sql = """
                SELECT id,
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
     * Motor principal de procesamiento de pagos Banco B con ejecución atómica y auditoría centralizada.
     */
    @Transactional(transactionManager = "bancoBTransactionManager")
    public int procesarPago(String pan, BigDecimal monto, String stan) {
        Map<String, Object> cuenta;

        // 1. Validar existencia de cuenta
        try {
            cuenta = buscarCuentaPorPan(pan);
        } catch (Exception e) {
            log.warn("Host Banco B: Cuenta no encontrada para PAN {}", MaskingUtils.maskPan(pan));
            return 14; // Código 14: Tarjeta no encontrada
        }

        Long cuentaId = ((Number) cuenta.get("id")).longValue();

        // 2. Validar estado de la cuenta
        String estado = String.valueOf(cuenta.get("estado"));
        if (!"ACTIVA".equalsIgnoreCase(estado)) {
            registrarAuditoria(cuentaId, monto, stan, "RECHAZADA", IsoConstants.RESPONSE_ACCOUNT_NOT_ACTIVE);
            return 57; // Código 57: Cuenta inactiva
        }

        // 3. Validar monto
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            registrarAuditoria(cuentaId, monto, stan, "RECHAZADA", IsoConstants.RESPONSE_INVALID_AMOUNT);
            return 13; // Código 13: Monto inválido
        }

        // 4. Validar saldo disponible
        BigDecimal saldo = (BigDecimal) cuenta.get("saldo");
        if (saldo.compareTo(monto) < 0) {
            registrarAuditoria(cuentaId, monto, stan, "RECHAZADA", IsoConstants.RESPONSE_INSUFFICIENT_FUNDS);
            return 51; // Código 51: Fondos insuficientes
        }

        // 5. Débito atómico con cláusula de salvaguarda
        String updateSaldo = """
                UPDATE cuentas
                SET saldo = saldo - ?
                WHERE id = ? AND saldo >= ?
                """;

        int filas = bancoBJdbcTemplate.update(updateSaldo, monto, cuentaId, monto);
        if (filas == 0) {
            registrarAuditoria(cuentaId, monto, stan, "RECHAZADA", IsoConstants.RESPONSE_INSUFFICIENT_FUNDS);
            return 51; // Fondos insuficientes por concurrencia
        }

        // 6. Registro de auditoría contable unificado
        registrarAuditoria(cuentaId, monto, stan, "APROBADA", "00");
        log.debug("Host Banco B: Pago aprobado exitosamente para PAN {}", MaskingUtils.maskPan(pan));
        return 0; // Aprobado
    }

    /**
     * Registrador centralizado de auditoría en la tabla transacciones de Banco B.
     */
    private void registrarAuditoria(Long cuentaId, BigDecimal monto, String stan, String estado, String codigoRespuesta) {
        String sql = """
                INSERT INTO transacciones
                    (cuenta_id, monto, tipo, estado, codigo_respuesta, stan, fecha_hora)
                VALUES
                    (?, ?, 'PAGO', ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        bancoBJdbcTemplate.update(
                sql,
                cuentaId,
                monto == null ? BigDecimal.ZERO : monto,
                estado,
                codigoRespuesta,
                stan
        );
    }
}
