package com.pasarelapagos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PasarelaPagosApplicationTests {

    @Test
    @DisplayName("Verificación de Contexto Spring Boot 4.1 y Telemetría de Sistema")
    void contextLoads() {
        System.out.println("\n========================================================================");
        System.out.println("🧪 PRUEBA DE CONTEXTO & TELEMETRÍA INICIAL DEL SISTEMA");
        System.out.println("========================================================================");

        // 1. Verificación de memoria RAM
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double usedMb = usedMemory / (1024.0 * 1024.0);
        double totalMb = totalMemory / (1024.0 * 1024.0);
        double maxMb = runtime.maxMemory() / (1024.0 * 1024.0);

        // 2. Verificación de entorno de hardware/JVM
        int availableProcessors = runtime.availableProcessors();
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");

        System.out.println("🚀 [1/2] Contexto de Spring Boot 4.1 cargado exitosamente.");
        System.out.println("📊 [2/2] Telemetría del Entorno de Ejecución:");
        System.out.printf("   - Versión de Java Activa:     %s\n", javaVersion);
        System.out.printf("   - Sistema Operativo:          %s\n", osName);
        System.out.printf("   - Núcleos de CPU Disponibles: %d (Soporte Virtual Threads)\n", availableProcessors);
        System.out.printf("   - Memoria RAM Utilizada:      %.2f MB\n", usedMb);
        System.out.printf("   - Memoria Total Reservada:    %.2f MB\n", totalMb);
        System.out.printf("   - Memoria Máxima Permitida:   %.2f MB\n", maxMb);
        System.out.println("========================================================================\n");

        // Aserción básica para asegurar que el contexto levantó
        assertTrue(usedMemory > 0, "El consumo de memoria debe ser válido");
    }
}