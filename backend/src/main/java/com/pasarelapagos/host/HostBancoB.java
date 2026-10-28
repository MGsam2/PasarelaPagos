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
 * Optimizado para alta concurrencia: consultas optimizadas y débito atómico.
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
     * Valida si la cuenta del Banco B puede realizar el pago.
     */
    public int validarPago(String pan, BigDecimal monto) {
        try {
            Map<String, Object> cuenta = buscarCuentaPorPan(pan);
            return validarCuentaYSaldo(cuenta, monto);
        } catch (Exception e) {
            return 14;
        }
    }

    private int validarCuentaYSaldo(Map<String, Object> cuenta, BigDecimal monto) {
        String estado = String.valueOf(cuenta.get("estado"));
        if (!"ACTIVA".equalsIgnoreCase(estado)) {
            return 57; // Código 57: Cuenta inactiva
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return 13; // Código 13: Monto inválido
        }

        BigDecimal saldo = (BigDecimal) cuenta.get("saldo");
        if (saldo.compareTo(monto) < 0) {
            return 51; // Código 51: Fondos insuficientes
        }

        return 0; // Validación exitosa
    }

    /**
     * Registra la transacción y descuenta el saldo atómicamente.
     */
    public void registrarTransaccion(
            String pan,
            BigDecimal monto,
            String stan,
            String codigoRespuesta,
            String estadoTransaccion) {

        Map<String, Object> cuenta = buscarCuentaPorPan(pan);
        Long cuentaId = ((Number) cuenta.get("id")).longValue();

        if ("00".equals(codigoRespuesta) || "0".equals(codigoRespuesta)) {
            bancoBJdbcTemplate.update(
                    "UPDATE cuentas SET saldo = saldo - ? WHERE id = ? AND saldo >= ?",
                    monto, cuentaId, monto
            );
        }

        String insertTransaccion = """
                INSERT INTO transacciones
                    (cuenta_id, monto, tipo, estado, codigo_respuesta, stan, fecha_hora)
                VALUES
                    (?, ?, 'PAGO', ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        bancoBJdbcTemplate.update(
                insertTransaccion,
                cuentaId,
                monto == null ? BigDecimal.ZERO : monto,
                estadoTransaccion,
                codigoRespuesta,
                stan
        );
    }

    /**
     * Motor principal de procesamiento de pagos Banco B con ejecución atómica de un solo paso.
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

        // 2. Validar reglas de negocio con la cuenta ya cargada (sin re-consultas SQL)
        int validacion = validarCuentaYSaldo(cuenta, monto);
        if (validacion != 0) {
            registrarRechazoDirecto(cuentaId, monto, stan, String.format("%02d", validacion));
            return validacion;
        }

        // 3. Débito atómico con cláusula de salvaguarda
        String updateSaldo = """
                UPDATE cuentas
                SET saldo = saldo - ?
                WHERE id = ? AND saldo >= ?
                """;

        int filas = bancoBJdbcTemplate.update(updateSaldo, monto, cuentaId, monto);
        if (filas == 0) {
            registrarRechazoDirecto(cuentaId, monto, stan, IsoConstants.RESPONSE_INSUFFICIENT_FUNDS);
            return 51; // Fondos insuficientes por concurrencia
        }

        // 4. Registro de auditoría contable
        String insertTransaccion = """
                INSERT INTO transacciones
                    (cuenta_id, monto, tipo, estado, codigo_respuesta, stan, fecha_hora)
                VALUES
                    (?, ?, 'PAGO', 'APROBADA', '00', ?, CURRENT_TIMESTAMP)
                """;

        bancoBJdbcTemplate.update(insertTransaccion, cuentaId, monto, stan);
        log.debug("Host Banco B: Pago aprobado exitosamente para PAN {}", MaskingUtils.maskPan(pan));
        return 0; // Aprobado
    }

    private void registrarRechazoDirecto(Long cuentaId, BigDecimal monto, String stan, String codigoRespuesta) {
        String insertTransaccion = """
                INSERT INTO transacciones
                    (cuenta_id, monto, tipo, estado, codigo_respuesta, stan, fecha_hora)
                VALUES
                    (?, ?, 'PAGO', 'RECHAZADA', ?, ?, CURRENT_TIMESTAMP)
                """;
        bancoBJdbcTemplate.update(
                insertTransaccion,
                cuentaId,
                monto == null ? BigDecimal.ZERO : monto,
                codigoRespuesta,
                stan
        );
    }
}
