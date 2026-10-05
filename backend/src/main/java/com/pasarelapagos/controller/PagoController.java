package com.pasarelapagos.controller;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.dto.PagoResponse;
import com.pasarelapagos.dto.PagoTarjetaRequest;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.exception.RecursoNoEncontradoException;
import com.pasarelapagos.repository.CuentaRepository;
import com.pasarelapagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final CuentaRepository cuentaRepository;

    public PagoController(
            PagoService pagoService,
            CuentaRepository cuentaRepository) {

        this.pagoService = pagoService;
        this.cuentaRepository = cuentaRepository;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> realizarPago(
            @Valid @RequestBody PagoRequest request) {

        Transaccion transaccion =
                pagoService.realizarPago(
                        request.getCuentaId(),
                        request.getMonto(),
                        request.getStan()
                );

        return ResponseEntity.ok(
                new PagoResponse(transaccion)
        );
    }

    @PostMapping("/tarjeta")
    public ResponseEntity<PagoResponse> realizarPagoConTarjeta(
            @Valid @RequestBody PagoTarjetaRequest request) {

        Cuenta cuenta =
                cuentaRepository.findByPan(request.getPan())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No existe una cuenta asociada a la tarjeta"
                                )
                        );

        Transaccion transaccion =
                pagoService.realizarPago(
                        cuenta.getId(),
                        request.getMonto(),
                        request.getStan()
                );

        return ResponseEntity.ok(
                new PagoResponse(transaccion)
        );
    }
}