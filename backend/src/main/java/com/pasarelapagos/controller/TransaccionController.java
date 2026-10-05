package com.pasarelapagos.controller;

import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.service.TransaccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(
            TransaccionService transaccionService) {

        this.transaccionService = transaccionService;
    }

    @GetMapping
    public ResponseEntity<List<Transaccion>>
    listarTransacciones() {

        return ResponseEntity.ok(
                transaccionService.listarTransacciones()
        );
    }

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<Transaccion>>
    listarPorCuenta(
            @PathVariable Long cuentaId) {

        return ResponseEntity.ok(
                transaccionService.listarPorCuenta(cuentaId)
        );
    }
}