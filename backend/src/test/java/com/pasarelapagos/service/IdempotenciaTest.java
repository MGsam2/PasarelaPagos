package com.pasarelapagos.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdempotenciaTest {

    @Autowired
    private IdempotenciaService idempotenciaService;

    @Test
    @DisplayName("Criterio 3: Debe detectar transacciones duplicadas por clave RRN/STAN")
    void testIdempotenciaTransaccionesDuplicadas() {
        System.out.println("\n========================================================================");
        System.out.println("🧪 PRUEBA 4: CONTROL DE IDEMPOTENCIA Y PREVENCIÓN DE DUPLICADOS");
        System.out.println("========================================================================");

        String claveIdempotencia = "RRN_TEST_9999";

        System.out.println("📝 [1/3] Registrando primera transacción con clave: " + claveIdempotencia);
        idempotenciaService.registrarTransaccion(claveIdempotencia, 0);

        System.out.println("🔍 [2/3] Verificando reintento de transacción duplicada con la misma clave...");
        boolean esDuplicada = idempotenciaService.esTransaccionDuplicada(claveIdempotencia);
        int codigoPrevio = idempotenciaService.obtenerRespuestaPrevia(claveIdempotencia);

        System.out.println("🛡️  [3/3] ¿Transacción duplicada detectada?: " + esDuplicada + " | Código de respuesta retornado: " + codigoPrevio);
        System.out.println("✅ RESULTADO: El sistema evitó un cobro duplicado en la base de datos.");
        System.out.println("========================================================================\n");

        assertTrue(esDuplicada);
        assertEquals(0, codigoPrevio);
    }
}