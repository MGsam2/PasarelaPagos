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

import java.math.BigDecimal;

/**
 * Controlador REST alternativo para procesamiento de cobros web vía JSON.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final CuentaRepository cuentaRepository;

    public PagoController(PagoService pagoService, CuentaRepository cuentaRepository) {
        this.pagoService = pagoService;
        this.cuentaRepository = cuentaRepository;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> realizarPago(@Valid @RequestBody PagoRequest request) {
        return ejecutarCobro(request.pan(), request.monto(), request.stan());
    }

    @PostMapping("/tarjeta")
    public ResponseEntity<PagoResponse> realizarPagoConTarjeta(@Valid @RequestBody PagoTarjetaRequest request) {
        return ejecutarCobro(request.pan(), request.monto(), request.stan());
    }

    /**
     * Lógica unificada de cobro transaccional.
     */
    private ResponseEntity<PagoResponse> ejecutarCobro(String pan, BigDecimal monto, String stan) {
        Cuenta cuenta = cuentaRepository.findByPan(pan)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta asociada a la tarjeta"));

        Transaccion transaccion = pagoService.realizarPago(cuenta.getId(), monto, stan);
        return ResponseEntity.ok(new PagoResponse(transaccion));
    }
}
