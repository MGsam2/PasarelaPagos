package com.pasarelapagos.repository;

import com.pasarelapagos.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
//hereda metodos crud  listos para usar
public interface ClienteRepository extends JpaRepository<Cliente, Long> {//indica que su pk es @Id es long
}