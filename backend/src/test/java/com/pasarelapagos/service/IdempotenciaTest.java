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

        System.out.println("📝 [1/4] Registrando primera transacción exitosa con clave: " + claveIdempotencia);
        idempotenciaService.registrarTransaccion(claveIdempotencia, 0);

        // Cronómetro de precisión para medir el rendimiento de la consulta de duplicados
        long startTime = System.nanoTime();
        
        System.out.println("🔍 [2/4] Verificando reintento de transacción duplicada con la misma clave...");
        boolean esDuplicada = idempotenciaService.esTransaccionDuplicada(claveIdempotencia);
        int codigoPrevio = idempotenciaService.obtenerRespuestaPrevia(claveIdempotencia);

        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        System.out.printf("⏱️  [3/4] Tiempo de Validación de Idempotencia en Base de Datos: %.3f ms\n", durationMs);
        System.out.println("🛡️  [4/4] ¿Transacción duplicada detectada?: " + esDuplicada + " | Código previo retornado: " + codigoPrevio);
        System.out.println("✅ RESULTADO: El sistema evitó con éxito un cobro duplicado y retornó el estado previo.");
        System.out.println("========================================================================\n");

        assertTrue(esDuplicada);
        assertEquals(0, codigoPrevio);
        assertTrue(durationMs < 1000, "La validación de idempotencia debe resolverse rápidamente");
    }
}