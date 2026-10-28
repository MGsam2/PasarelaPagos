package com.pasarelapagos.controller;

import com.pasarelapagos.dto.CuentaResponse;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.service.CuentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/* Mapea las solicitudes HTTP relacionadas con las cuentas bancarias 
 y las transforma a DTOs de salida (CuentaResponse)*/
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }
     /*Recibe clienteId, numeroCuenta, pan y saldoInicial. 
     Retorna la cuenta creada empaquetada en CuentaResponse*/
    @PostMapping
    public ResponseEntity<CuentaResponse> crearCuenta(
            @RequestParam Long clienteId,
            @RequestParam String numeroCuenta,
            @RequestParam String pan,
            @RequestParam BigDecimal saldoInicial) {

        Cuenta cuenta =
                cuentaService.crearCuenta(
                        clienteId,
                        numeroCuenta,
                        pan,
                        saldoInicial
                );

        return ResponseEntity.ok(
                new CuentaResponse(cuenta)
        );
    }

    /*Obtiene la lista de entidades Cuenta y las convierte a una lista de CuentaResponse 
     utilizando Java Streams y el constructor de mapeo.*/
    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listarCuentas() {

        List<CuentaResponse> cuentas =
                cuentaService.listarCuentas()
                        .stream()
                        .map(CuentaResponse::new)
                        .toList();

        return ResponseEntity.ok(cuentas);
    }

    /*Retorna la información de una cuenta específica en formato JSON*/
    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> buscarCuenta(
            @PathVariable Long id) {

        Cuenta cuenta =
                cuentaService.buscarPorId(id);

        return ResponseEntity.ok(
                new CuentaResponse(cuenta)
        );
    }
}
/*El CuentaController mapea los endpoints REST en /api/cuentas. 
Utiliza CuentaService para gestionar el ciclo de vida de las cuentas 
y transforma las entidades JPA en DTOs CuentaResponse para evitar acoplar 
la base de datos con la capa de presentación. */