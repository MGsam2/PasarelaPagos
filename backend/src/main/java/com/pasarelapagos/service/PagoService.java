package com.pasarelapagos.service;

import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.repository.CuentaRepository;
import com.pasarelapagos.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Transactional
    public Transaccion realizarPago(
            Long cuentaId,
            BigDecimal monto,
            String stan) {

        // 1. Buscar la cuenta
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new RuntimeException("Cuenta no encontrada"));

        // 2. Verificar que la cuenta esté activa
        if (!"ACTIVA".equals(cuenta.getEstado())) {

            return registrarRechazo(
                    cuenta,
                    monto,
                    stan,
                    "57"
            );
        }

        // 3. Verificar que el monto sea válido
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {

            return registrarRechazo(
                    cuenta,
                    monto,
                    stan,
                    "13"
            );
        }

        // 4. Verificar saldo
        if (cuenta.getSaldo().compareTo(monto) < 0) {

            return registrarRechazo(
                    cuenta,
                    monto,
                    stan,
                    "51"
            );
        }

        // 5. Descontar el dinero
        cuenta.setSaldo(
                cuenta.getSaldo().subtract(monto)
        );

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