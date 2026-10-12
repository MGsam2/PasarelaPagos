package com.pasarelapagos.corebancario;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.ruteador.RuteadorPagoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CoreBancarioClient {

    private final RestClient restClient;

    public CoreBancarioClient(
            RestClient.Builder restClientBuilder,
            @Value("${corebancario.url:http://localhost:8080}") String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public RuteadorPagoResponse procesarPagoCore(PagoRequest pagoRequest) {
        try {
            return restClient.post()
                    .uri("/api/ruteador/pago")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("pan=" + pagoRequest.pan() +
                          "&monto=" + pagoRequest.monto() +
                          "&stan=" + pagoRequest.stan())
                    .retrieve()
                    .body(RuteadorPagoResponse.class);
        } catch (Exception e) {
            System.err.println("Error de comunicación con Core Bancario vía RestClient: " + e.getMessage());
            return new RuteadorPagoResponse("DESCONOCIDO", 96, "ERROR_CORE");
        }
    }
}