package com.pasarelapagos.config;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente HTTP RestClient (Spring Framework 7 / Spring Boot 4.1).
 * Emplea estándares de inyección y ciclo de vida de Jakarta EE 11 (jakarta.annotation.* y jakarta.inject.*).
 */
@Configuration
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    @PostConstruct
    public void init() {
        log.info("Inicializando configuración de RestClient con especificación Jakarta EE 11");
    }

    @Bean
    @Named("globalRestClientBuilder")
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
