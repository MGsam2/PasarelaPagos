package com.pasarelapagos.service;

import com.pasarelapagos.entity.Cliente;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.exception.RecursoNoEncontradoException;
import com.pasarelapagos.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import com.pasarelapagos.exception.RecursoNoEncontradoException;
import java.math.BigDecimal;
import java.util.List;

/*SERVICIO DE GESTION DE CUENTAS: administra el ciclo de vida de las cuentas bancarias
(Asociacion con cliente, pan y saldo inicial) */
@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteService clienteService;
    //constructor
    public CuentaService(
            CuentaRepository cuentaRepository,
            ClienteService clienteService) {

        this.cuentaRepository = cuentaRepository;
        this.clienteService = clienteService;
    }
    /*APERTURA DE CUENTA: busca titular, crea la entidad Cuenta con estado 'ACTIVA' por defecto
    y la guarada en PSQL mediante CuentaRespository */
    public Cuenta crearCuenta(
            Long clienteId,
            String numeroCuenta,
            String pan,
            BigDecimal saldoInicial) {
        //1.Valida y recupera el cliente dueño de la cuenta
        Cliente cliente = clienteService.buscarPorId(clienteId);
        //2. Construye la nueva cuenta asignandole estado ACTIVA
        Cuenta cuenta = new Cuenta(
                cliente,
                numeroCuenta,
                pan,
                saldoInicial,
                "ACTIVA"
        );
        //3.Persiste en PSQL
        return cuentaRepository.save(cuenta);
    }
    //LISTA CUENTAS: Retorna todas las cuentas registradas en el sistema
    public List<Cuenta> listarCuentas() {

        return cuentaRepository.findAll();
    }
    /*BUSCAR POR ID, consulta la cuenta por su Clave Primaria, si no existe
    Lanza RecursoNoencontradoException */
    public Cuenta buscarPorId(Long id) {

        return cuentaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cuenta no encontrada"
                        ));
    }
}
/*"El CuentaService gestiona las cuentas bancarias y su vinculación con los clientes. 
Al crear una cuenta, reutiliza el ClienteService para verificar la existencia del titular, 
asigna el PAN, número de cuenta, saldo inicial y el estado 'ACTIVA', 
persistiendo el resultado a través de CuentaRepository." */