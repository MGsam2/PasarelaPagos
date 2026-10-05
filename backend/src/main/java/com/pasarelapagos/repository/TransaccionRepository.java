package com.pasarelapagos.repository;

import com.pasarelapagos.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionRepository
        extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByCuentaIdOrderByFechaHoraDesc(
            Long cuentaId
    );
}