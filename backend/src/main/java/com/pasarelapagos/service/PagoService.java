package com.pasarelapagos.service;

import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.repository.CuentaRepository;
import com.pasarelapagos.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/*SERVICIO CORE DE PAGOS JPA
procesa las transacciones de debito garantiza consistencia ACID
mediante la notacion @Transactional */
@Service
public class PagoService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    public PagoService(
            CuentaRepository cuentaRepository,
            TransaccionRepository transaccionRepository) {

        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }
/*PROCESA PAGO TRANSACCIONAL La anotacion @Transactional garantiza la propiedad ACID. Si ocurre un error inesperado,
     * Spring realiza un rollback de los cambios tanto en la cuenta como en la transaccion.
 */
    @Transactional//encierra todo el metodo realizarPago
    public Transaccion realizarPago(
            Long cuentaId,
            BigDecimal monto,
            String stan) {

        // 1. Buscar la cuenta en BD, lanza excepcion si no existe
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new RuntimeException("Cuenta no encontrada"));

        // 2. Verificar que la cuenta esté activa
        if (!"ACTIVA".equals(cuenta.getEstado())) {

            return registrarRechazo(
                    cuenta,
                    monto,
                    stan,
                    "57" //codigo iso 57 cuenta inactiva o bloqueada
            );
        }

        // 3. Verificar que el monto sea válido mayor a cero 
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {

            return registrarRechazo(
                    cuenta,
                    monto,
                    stan,
                    "13"//iso 13 monto invalido
            );
        }

        // 4. Verificar disponibilidad de saldo suficiente
        if (cuenta.getSaldo().compareTo(monto) < 0) {

            return registrarRechazo(
                    cuenta,
                    monto,
                    stan,
                    "51" //fondo insuficiente
            );
        }

        // 5. Descontar el dinero - resta el monto del saldo de la cuenta
        cuenta.setSaldo(
                cuenta.getSaldo().subtract(monto)
        );
        // actualiza la cuenta en PSQL mediante hibernate
        cuentaRepository.save(cuenta);

        // 6. Registrar transacción aprobada
        Transaccion transaccion = new Transaccion(
                cuenta,
                monto,
                "PAGO",
                "APROBADA",
                "00",
                stan,
                LocalDateTime.now()
        );

        return transaccionRepository.save(transaccion);
    }

    //Registro de rechazo guarda en transacciones en PSQL la transaccion fallida
    private Transaccion registrarRechazo(
            Cuenta cuenta,
            BigDecimal monto,
            String stan,
            String codigoRespuesta) {

        Transaccion transaccion = new Transaccion(
                cuenta,
                monto,
                "PAGO",
                "RECHAZADA",
                codigoRespuesta,
                stan,
                LocalDateTime.now()
        );

        return transaccionRepository.save(transaccion);
    }
}

/*La anotación @Transactional de Spring 
encierra todo el método realizarPago en un bloque de transacción SQL de PostgreSQL. 
Si el sistema se cae o falla el save() del paso 6, el descuento del paso 5 no se consolida 
(se deshace con Rollback), previniendo pérdida de dinero o estados inconsistentes. */

/*Diferencia entre PagoService y los HostBanco:
PagoService opera mediante JPA / ORM sobre entidades orientadas a objetos (Cuenta, Transaccion).
Los HostBancoA y HostBancoB operan mediante JdbcTemplate (SQL Nativo) 
para maximizar la velocidad de respuesta del Ruteador en transacciones masivas ISO 8583. */

/*"El PagoService orquesta el pago a nivel de ORM con JPA. 
Utiliza la anotación @Transactional para asegurar consistencia ACID durante el débito de saldo. 
Valida en secuencia las reglas de negocio (estado activo, monto válido y fondos suficientes), 
devolviendo transacciones registradas como APROBADA (00) o RECHAZADA (57, 13, 51)." 

"El PagoService es el motor de procesamiento local de la pasarela. 
Opera sobre el ORM (JPA/Hibernate) y garantiza la integridad financiera mediante la anotación 
@Transactional, cumpliendo con las propiedades ACID para evitar pérdida de fondos ante 
caídas del sistema. Ejecuta un pipeline estricto de validación de negocio 
(estado, montos y fondos), mapeando cada rechazo a su respectivo código de respuesta del 
estándar ISO 8583 (como el 51 o 57). A diferencia de los componentes de ruteo masivo que 
usan JDBC Nativo por velocidad, este servicio prioriza la consistencia estructural del dominio."*/