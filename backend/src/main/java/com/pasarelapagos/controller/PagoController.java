package com.pasarelapagos.controller;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.dto.PagoResponse;
import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> realizarPago(
            @Valid @RequestBody PagoRequest request) {

        Transaccion transaccion = pagoService.realizarPago(
                request.getCuentaId(),
                request.getMonto(),
                request.getStan()
        );

        return ResponseEntity.ok(
                new PagoResponse(transaccion)
        );
    }
}