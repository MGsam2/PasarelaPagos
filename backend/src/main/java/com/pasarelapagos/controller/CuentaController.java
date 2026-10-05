package com.pasarelapagos.controller;

import com.pasarelapagos.dto.CuentaResponse;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.service.CuentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> buscarCuenta(
            @PathVariable Long id) {

        Cuenta cuenta = cuentaService.buscarPorId(id);

        return ResponseEntity.ok(
                new CuentaResponse(cuenta)
        );
    }
}