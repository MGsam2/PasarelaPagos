package com.pasarelapagos.corebancario;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.ruteador.RuteadorPagoResponse;
import com.pasarelapagos.util.MaskingUtils;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP resiliente para el Core Bancario basado en RestClient.
 * Aprovecha estándares de inyección Jakarta EE 11 (jakarta.inject.Inject, jakarta.inject.Named).
 */
@Component
@Named("coreBancarioClient")
public class CoreBancarioClient {

    private static final Logger log = LoggerFactory.getLogger(CoreBancarioClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    @Inject
    public CoreBancarioClient(
            @Named("globalRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${corebancario.url:http://localhost:8080}") String baseUrl) {

        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @PostConstruct
    public void onInit() {
        log.info("CoreBancarioClient inicializado con URL base: {}", baseUrl);
    }

    /**
     * Procesa la transacción hacia el Core Bancario con captura y traducción de fallos a código ISO 96.
     */
    public RuteadorPagoResponse procesarPagoCore(PagoRequest pagoRequest) {
        try {
            log.debug("Enviando pago a Core Bancario para PAN: {}", MaskingUtils.maskPan(pagoRequest.pan()));
            return restClient.post()
                    .uri("/api/ruteador/pago")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("pan=" + pagoRequest.pan() +
                          "&monto=" + pagoRequest.monto() +
                          "&stan=" + pagoRequest.stan())
                    .retrieve()
                    .body(RuteadorPagoResponse.class);
        } catch (Exception e) {
            log.error("Fallo de comunicación HTTP con Core Bancario vía RestClient: {}", e.getMessage());
            return new RuteadorPagoResponse("DESCONOCIDO", 96, "ERROR_CORE - Error de comunicación del sistema");
        }
    }
}
