package com.pasarelapagos.controller;

import com.pasarelapagos.dto.CuentaResponse;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.service.CuentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

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

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listarCuentas() {

        List<CuentaResponse> cuentas =
                cuentaService.listarCuentas()
                        .stream()
                        .map(CuentaResponse::new)
                        .toList();

        return ResponseEntity.ok(cuentas);
    }

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