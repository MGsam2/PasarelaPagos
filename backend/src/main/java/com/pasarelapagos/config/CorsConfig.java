package com.pasarelapagos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*CONFIGURACIÓN DE CORS (Cross-Origin Resource Sharing):
 Permite el consumo de la API REST desde aplicaciones cliente ejecutándose en dominios/puertos distintos*/
@Configuration
public class CorsConfig {

    /*REGISTRO DE REGLAS CORS:
     Retorna un WebMvcConfigurer que personaliza la política global de Spring MVC*/
    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                // addMapping("/**"): Aplica la regla a todas las rutas de la API (/api/clientes, /api/pagos, etc.)
                registry.addMapping("/**")
                // allowedOrigins: Habilita solicitudes desde el servidor de desarrollo del Frontend (React + Vite)
                        .allowedOrigins("http://localhost:5173")
                        // allowedMethods: Define los verbos HTTP autorizados
                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "OPTIONS" // Requerido para peticiones preflight del navegador
                        )
                        // allowedHeaders("*"): Permite cualquier tipo de cabecera personalizada (Authorization, Content-Type, etc.)
                        .allowedHeaders("*");
            }
        };
    }
}
/*El CorsConfig habilita el acceso Cross-Origin a través de WebMvcConfigurer. 
Configura la política CORS global para permitir que la aplicación Frontend en React 
(ejecutándose en localhost:5173) consuma los endpoints de la pasarela mediante 
peticiones HTTP GET, POST, PUT, DELETE y OPTIONS */