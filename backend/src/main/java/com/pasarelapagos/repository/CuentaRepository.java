package com.pasarelapagos.repository;

import com.pasarelapagos.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
//hereda metodos crud  listos para usar
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    //consulta de busqueda por medio del pan select from cuenta where pan = ?
    //optional viene vacio  o no existe, permite lanzar una excepcion personalizada que responde el codigo iso eje: 14 tarjeta no encontrada
    Optional<Cuenta> findByPan(String pan);
}