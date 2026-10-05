package com.pasarelapagos.repository;

import com.pasarelapagos.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByPan(String pan);
}