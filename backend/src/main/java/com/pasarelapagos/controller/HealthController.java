package com.pasarelapagos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*CONTROLADOR DE SALUD (HEALTH CHECK):
 Expone un endpoint liviano para monitorear el estado de disponibilidad del microservicio*/
@RestController
public class HealthController {

    /*Devuelve una confirmación directa de que la aplicación está activa y respondiendo*/
    @GetMapping("/api/health")
    public String health() {
        return "Pasarela de Pagos funcionando";
    }
}
/*El HealthController expone el endpoint /api/health 
para comprobaciones de estado y monitoreo. 
Funciona como un Health Check de alta velocidad para verificar 
la disponibilidad del microservicio en entornos en la nube*/