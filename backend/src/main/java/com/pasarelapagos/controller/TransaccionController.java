package com.pasarelapagos.controller;

import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.service.TransaccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/* CONTROLADOR REST DE AUDITORÍA Y HISTORIAL:
 * Mapea las solicitudes HTTP GET para consultar el historial completo 
 * de cobros aprobados y rechazados registrados en la pasarela*/
@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(
            TransaccionService transaccionService) {

        this.transaccionService = transaccionService;
    }

    /* HISTORIAL GLOBAL (HTTP GET /api/transacciones):
     Retorna el listado completo de todas las transacciones procesadas por el sistema*/
    @GetMapping
    public ResponseEntity<List<Transaccion>>
    listarTransacciones() {

        return ResponseEntity.ok(
                transaccionService.listarTransacciones()
        );
    }

    /*ESTADO DE CUENTA ESPECÍFICO (HTTP GET /api/transacciones/cuenta/{cuentaId}):
     Extrae el ID de la cuenta desde la URL (@PathVariable) y retorna sus movimientos
     ordenados cronológicamente desde el más reciente*/
    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<Transaccion>>
    listarPorCuenta(
            @PathVariable Long cuentaId) {

        return ResponseEntity.ok(
                transaccionService.listarPorCuenta(cuentaId)
        );
    }
}

/*El TransaccionController expone los endpoints de auditoría bajo /api/transacciones. 
Permite listar el historial global o filtrar por cuenta específica en /cuenta/{cuentaId}, 
devolviendo los registros directamente desde TransaccionService */