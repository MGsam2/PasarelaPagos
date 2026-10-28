package com.pasarelapagos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/*CONFIGURACIÓN DEL CLIENTE HTTP (REST CLIENT):
 Centraliza la creación del motor de peticiones HTTP salientes introducido en Spring 3.2+
 para comunicarse con sistemas externos (como los cores bancarios).*/
@Configuration
public class RestClientConfig {

    /*CONSTRUCTOR DE CLIENTE REST GLOBAL:
     Expone un RestClient.Builder administrado por Spring.
     Permite que cualquier componente de la pasarela instancie clientes HTTP 
     heredando automáticamente las configuraciones globales (métricas, trazabilidad).*/
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
/*"El RestClientConfig provee un RestClient.Builder gestionado por el contenedor de Inversión de Control (IoC). 
Esta decisión arquitectónica reemplaza al antiguo RestTemplate, ofreciendo una API fluida y optimizada. 
Al inyectar el Builder (y no el cliente estático), aseguramos que todas las peticiones HTTP salientes 
puedan integrarse automáticamente con el ecosistema de observabilidad y seguridad de Spring Boot."*/