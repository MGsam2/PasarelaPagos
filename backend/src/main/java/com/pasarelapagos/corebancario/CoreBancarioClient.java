package com.pasarelapagos.corebancario;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.ruteador.RuteadorPagoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/*CLIENTE HTTP DEL CORE BANCARIO
utiliza RestClient de Spring Boot para realizar llamada HTTP sincronas de alta velocidad */
@Component
public class CoreBancarioClient {

    private final RestClient restClient;

    /*CONSTRUCTOR CON RestClient.Builder:
    Inyecta la URL base definida en application.properties o usa localhost:8080 por defecto.*/
    public CoreBancarioClient(
            RestClient.Builder restClientBuilder,
            @Value("${corebancario.url:http://localhost:8080}") String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    /*PROCESAR PAGO VIA HTTP
    transmite los parametros de pago mediante peticion HTTP POST codificada en URL form */
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
            /*Manejo de fallo de comunicacion:
            Si la llamada HTTP falla, retorna codigo ISO 96 (Error del sistema) */
            System.err.println("Error de comunicación con Core Bancario vía RestClient: " + e.getMessage());
            return new RuteadorPagoResponse("DESCONOCIDO", 96, "ERROR_CORE - Error del sistema");
        }
    }
}
/*Por qué usar RestClient en lugar de RestTemplate?

RestClient es la API de cliente HTTP sincrónico introducida en Spring Framework 6.1 / Spring Boot 3+. 
Ofrece una interfaz fluida (fluent API) sin bloqueos innecesarios, 
completamente compatible y optimizada para ejecutarse sobre Virtual Threads de Java 21.

Manejo de Resiliencia (Código ISO 96):

Si el servicio remoto no responde o la red colapsa, 
el bloque catch previene que la aplicación crashee y responde con el código financiero 96 (System Error), 
garantizando estabilidad.

"El CoreBancarioClient utiliza la API fluida RestClient de Spring para comunicarse vía HTTP POST con el endpoint del ruteador. 
Procesa peticiones codificadas en URL Form y gestiona fallos de red devolviendo el código de error de sistema ISO 96." */