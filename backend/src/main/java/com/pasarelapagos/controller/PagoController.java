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

/* Expone endpoints HTTP POST (/api/pagos) para procesar cobros web en formato JSON*/
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

    /*Recibe un PagoRequest JSON en el cuerpo de la petición.
     La anotación @Valid ejecuta la validación automática de Bean Validation (@NotBlank, @Positive)*/
    @PostMapping
    public ResponseEntity<PagoResponse> realizarPago(
            @Valid @RequestBody PagoRequest request) {

        //1. Busca la cuenta por el PAN en la BD
        Cuenta cuenta =
                cuentaRepository.findByPan(request.pan())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No existe una cuenta asociada al PAN"
                                )
                        );

        //2. Delega el debito transaccional a PagoService
        Transaccion transaccion =
                pagoService.realizarPago(
                        cuenta.getId(),
                        request.monto(),
                        request.stan()
                );

        //3. Empaca la entidad Transaccion en el DTO PagoResponse para la respuesta JSON
        return ResponseEntity.ok(
                new PagoResponse(transaccion)
        );
    }

    /* Endpoint alternativo que recibe un PagoTarjetaRequest con campos adicionales de tarjeta.*/
    @PostMapping("/tarjeta")
    public ResponseEntity<PagoResponse> realizarPagoConTarjeta(
            @Valid @RequestBody PagoTarjetaRequest request) {

        Cuenta cuenta =
                cuentaRepository.findByPan(request.pan())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No existe una cuenta asociada a la tarjeta"
                                )
                        );

        Transaccion transaccion =
                pagoService.realizarPago(
                        cuenta.getId(),
                        request.monto(),
                        request.stan()
                );

        return ResponseEntity.ok(
                new PagoResponse(transaccion)
        );
    }
}
/*El PagoController procesa las peticiones de cobro web en /api/pagos. 
Valida los DTOs de entrada con @Valid, recupera la cuenta mediante CuentaRepository 
a través del PAN y ejecuta el débito en PagoService, retornando la respuesta 
estandarizada en un DTO PagoResponse*/