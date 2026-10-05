package com.pasarelapagos.service;

import com.pasarelapagos.entity.Cliente;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.exception.RecursoNoEncontradoException;
import com.pasarelapagos.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import com.pasarelapagos.exception.RecursoNoEncontradoException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteService clienteService;

    public CuentaService(
            CuentaRepository cuentaRepository,
            ClienteService clienteService) {

        this.cuentaRepository = cuentaRepository;
        this.clienteService = clienteService;
    }

    public Cuenta crearCuenta(
            Long clienteId,
            String numeroCuenta,
            String pan,
            BigDecimal saldoInicial) {

        Cliente cliente = clienteService.buscarPorId(clienteId);

        Cuenta cuenta = new Cuenta(
                cliente,
                numeroCuenta,
                pan,
                saldoInicial,
                "ACTIVA"
        );

        return cuentaRepository.save(cuenta);
    }

    public List<Cuenta> listarCuentas() {

        return cuentaRepository.findAll();
    }

    public Cuenta buscarPorId(Long id) {

        return cuentaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cuenta no encontrada"
                        ));
    }
}