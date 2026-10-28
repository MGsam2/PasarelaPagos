package com.pasarelapagos.host;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;

/*Si el Ruteador entregó la carta a la ventanilla de Banco A, el HostBancoA es el cajero del banco
HOST EMISOR BANCO A
procesa las transacciones de las tarjetas que inician con el BIN 4111
Utiliza mainJbdcTemplate para conectarse directamente a la BD principal*/
@Service
public class HostBancoA {

    private final JdbcTemplate jdbcTemplate;

    public HostBancoA(
            //indica a spring que use el pool de conexiones hacia la base de datos de Banco A
            @Qualifier("mainJdbcTemplate")
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;

        System.out.println("========================================");
        System.out.println("HOST BANCO A INICIALIZADO");
        System.out.println("JdbcTemplate: MAIN");
        System.out.println("========================================");
    }
     /**
      BUSCAR CUENTA: Ejecuta consulta SQL obteniendo saldo y estado por PAN.
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
/*MOTOR DE AUTORIZACION BANCARIA
Aplica las reglas de neegocio en orden y devuelve el codigo ISO correspondiente */
    public int procesarPago(
            String pan,
            BigDecimal monto,
            String stan) {

        Map<String, Object> cuenta;
//1.Verificar existencia de la tarjeta 
        try {

            cuenta = buscarCuentaPorPan(pan);

        } catch (Exception e) {

            System.err.println("========================================");
            System.err.println("ERROR EN HOST BANCO A");
            System.err.println("PAN: " + pan);
            System.err.println("MENSAJE: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
                //Si la consulta SQL no devuleve nada, retorna  codigo 14 - Tarjeta invalida
            return 14;
        }

        // 2. Verifica que el monto sea mayor a 0
        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {

            registrarTransaccionRechazada(
                    cuenta,
                    monto,
                    stan,
                    "13"
            );

            return 13;//Código 13 - monto inválido
        }

        //3.Verifica estado de la cuenta 
        String estado =
                String.valueOf(cuenta.get("estado"));

        if (!"ACTIVA".equalsIgnoreCase(estado)) {

            registrarTransaccionRechazada(
                    cuenta,
                    monto,
                    stan,
                    "57"
            );

            return 57;//Código 57 - cuenta inactiva
        }

        //4.Verifica disponibilidad de saldo  
        BigDecimal saldo =
                (BigDecimal) cuenta.get("saldo");

        if (saldo.compareTo(monto) < 0) {

            registrarTransaccionRechazada(
                    cuenta,
                    monto,
                    stan,
                    "51"
            );

            return 51; //Código 51 - fondos insuficientes
        }

        //5.Aprobacion, aplica el debito y registra la transaccion 
        registrarTransaccion(
                pan,
                monto,
                stan,
                "00",
                "APROBADA"
        );

        return 0;//Código 0 / 00 - pago aprobado
    }
    /*DEBITO CONTABLE Y REGISTRO EXITOSO
     resta el dinero del saldo de la cuenta e inserta la auditoria en transacciones */
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
        //actualiza el saldo en la tabla de cuentas de Banco A
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
        //inserta el comprobante en la tabla de transacciones de Banco A
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
    /*AUDITORIA DE RECHAZOS
    guarda en la BD las transacciones rechazadas para trazabilidad */
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