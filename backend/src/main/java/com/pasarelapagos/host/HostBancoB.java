package com.pasarelapagos.host;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;
/*Si el Ruteador entregó la carta a la ventanilla de Banco A, el HostBancoA es el cajero del banco
HOST EMISOR BANCO B
procesa las transacciones de las tarjetas que inician con el BIN 5222
Utiliza mainJbdcTemplate para conectarse directamente a la BD Banco B*/
@Component
public class HostBancoB {

    private final JdbcTemplate bancoBJdbcTemplate;
     /**
      @Qualifier("bancoBJdbcTemplate"): Inyecta el pool de conexiones 
     configurado específicamente para la base de datos de Banco B.
     */
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
     Consulta la cuenta bancaria por PAN en la BD de Banco B
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
            return 57;// Código 57: Cuenta inactiva
        }

        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {
            return 13;// Código 13: Monto inválido
        }

        BigDecimal saldo =
                (BigDecimal) cuenta.get("saldo");

        if (saldo.compareTo(monto) < 0) {
            return 51;// Código 51: Fondos insuficientes
        }

        return 0;// Validación exitosa
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
     MOTOR PRINCIPAL DE PROCESAMIENTO BANCO B:
     Ejecuta el pipeline completo de autorización y auditoría.
     */
    public int procesarPago(
            String pan,
            BigDecimal monto,
            String stan) {

        Map<String, Object> cuenta;

        // 1. Validar existencia de cuenta (Error = Código 14)
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

        // 2. Validar monto (Error = Código 13)
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

        // 3. Validar estado de la cuenta (Error = Código 57)
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

        //4.Validar saldo disponible (Error: Codigo 51)
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

        //5. Aprobacion y debito exitoso (Codigo 00 / 0)
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
/*"El HostBancoB implementa la lógica de autorización para las tarjetas procesadas en Banco B (BIN 5222). 
La diferencia clave con Banco A es técnica: utiliza @Qualifier("bancoBJdbcTemplate") para garantizar el aislamiento de datos, 
realizando consultas, débitos y registros de auditoría sobre la base de datos independiente banco_b." */