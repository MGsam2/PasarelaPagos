package com.pasarelapagos.ruteador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/*Si el Ruteador es el conmutador interno, el RuteadorController es la ventanilla de atención HTTP:
CONTROLADOR REST DEL RUTEADOR
expone endpoint http post api/ruteador/pago para probar o realizar
el enrutamiento financiero hacia banco A o B
*/
@RestController
@RequestMapping("/api/ruteador")
@CrossOrigin(origins = "*") //permite peticiones desde el front sin bloqueos CORS
public class RuteadorController {

    private final Ruteador ruteador;

    public RuteadorController(Ruteador ruteador) {
        this.ruteador = ruteador;
    }
     /**
     * ENDPOINT DE PAGO DIRECTO:
     * Recibe los parámetros obligatorios en la URL/Formulario (@RequestParam):
     */
    @PostMapping("/pago")
    public ResponseEntity<RuteadorPagoResponse> realizarPago(
            @RequestParam @NotBlank String pan,
            @RequestParam @NotNull @Positive BigDecimal monto,
            @RequestParam @NotBlank String stan) {
        //1. identifica el banco emisor a traves del BIN
        String banco = ruteador.determinarBanco(pan);
        //2.Procesa la transaccion y obtiene el codigo numerico (0 exito, !=0 para rechazo)
        int codigo = ruteador.procesarPago(
                pan,
                monto,
                stan
        );
        //3.Traduce el codigo numerico a un estado legible
        String estado = codigo == 0
                ? "APROBADA"
                : "RECHAZADA";
        //4.Retorna la respuesta HTTP 200 OK con el DTO JSON
        return ResponseEntity.ok(
                new RuteadorPagoResponse(
                        banco,
                        codigo,
                        estado
                )
        );
    }
}
/*"El RuteadorController expone el servicio de enrutamiento a través de la API REST (/api/ruteador/pago).
Recibe los parámetros validados (PAN, monto y STAN), delega el procesamiento al Ruteador 
y devuelve un JSON inmutable respaldado por RuteadorPagoResponse especificando el banco autorizador, 
el código de respuesta y el estado de la operación."*/