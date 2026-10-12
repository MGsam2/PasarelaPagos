package com.pasarelapagos.ruteador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/ruteador")
@CrossOrigin(origins = "*")
public class RuteadorController {

    private final Ruteador ruteador;

    public RuteadorController(Ruteador ruteador) {
        this.ruteador = ruteador;
    }

    @PostMapping("/pago")
    public ResponseEntity<RuteadorPagoResponse> realizarPago(
            @RequestParam @NotBlank String pan,
            @RequestParam @NotNull @Positive BigDecimal monto,
            @RequestParam @NotBlank String stan) {

        String banco = ruteador.determinarBanco(pan);

        int codigo = ruteador.procesarPago(
                pan,
                monto,
                stan
        );

        String estado = codigo == 0
                ? "APROBADA"
                : "RECHAZADA";

        return ResponseEntity.ok(
                new RuteadorPagoResponse(
                        banco,
                        codigo,
                        estado
                )
        );
    }
}