package com.pasarelapagos.service;

import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    public TransaccionService(
            TransaccionRepository transaccionRepository) {

        this.transaccionRepository = transaccionRepository;
    }

    public List<Transaccion> listarTransacciones() {

        return transaccionRepository.findAll();
    }

    public List<Transaccion> listarPorCuenta(
            Long cuentaId) {

        return transaccionRepository
                .findByCuentaIdOrderByFechaHoraDesc(cuentaId);
    }
}