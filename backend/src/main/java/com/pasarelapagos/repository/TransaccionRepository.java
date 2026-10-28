package com.pasarelapagos.repository;

import com.pasarelapagos.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
//hereda metodos crud  listos para usar
public interface TransaccionRepository
        extends JpaRepository<Transaccion, Long> {
//consulta derivada por nombre filtra la tabla transacciones por el id de la cuenta asociada cuenta_id=?
    List<Transaccion> findByCuentaIdOrderByFechaHoraDesc(//ordena de forma descendente, las transacciones +recientes primero
            Long cuentaId
    );
}