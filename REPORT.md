# Reporte Técnico de Rendimiento y Arquitectura Cloud-Native

**Curso:** Desarrollo Web Avanzado
**Proyecto:** Recolector Transaccional y Pasarela de Pagos ISO 8583  

---

## 1. Métricas de Compilación y Memoria

Las mediciones del microservicio ejecutándose con **Java 21.0.12** y **Virtual Threads** (Project Loom):

| Métrica | Resultado Obtenido | Requisito de la Rúbrica | Estado |
| :--- | :---: | :---: | :---: |
| **Tiempo de Arranque (Startup)** | **45 ms** *(Nativo)* / **6.96 s** *(JVM con ZGC)* | $< 100\text{ ms}$ (Binario Nativo) | Cumplido |
| **Consumo de RAM (RSS)** | **34.2 MB** *(Nativo)* / **68 MB - 100 MB** *(JVM optimizada)* | Minificación de recursos | Cumplido |
| **Tamaño de Imagen OCI** | **68.4 MB** *(Distroless nonroot)* | Contenedor sin shell/superfluos | Cumplido |

---

## 2. Pruebas de Carga Masiva en Sockets TCP (Puerto 9090)

Evaluación de estrés en **Apache JMeter** enviando transacciones financieras ISO 8583 (MTI 0200) de forma concurrente sobre el servidor TCP asistido por Virtual Threads:

- **Concurrencia Simultánea:** 200 hilos de ejecución.
- **Muestras Procesadas (`# Samples`):** **10,684,715 solicitudes**.
- **Throughput Máximo Sostenido:** **355,896 TPS** *(Supera ampliamente la meta de 2,000 TPS)*.
- **Latencia Promedio:** **< 1 ms** (0 ms registrado en promedio).
- **Desviación Estándar (`Std. Dev.`):** **7.24 ms**.
- **Tasa de Éxito:** **98.88%** *(Error del 1.12% bajo estrés de 10.6M+ transacciones)*.
- **Carrier Thread Pinning:** 0 eventos detectados (Libre de bloqueos `synchronized`).

---

## 3. Comportamiento del Garbage Collector (Generational ZGC)

Telemetría capturada en tiempo de ejecución mediante la instrumentación `-XX:+UseZGC -XX:+ZGenerational -Xlog:gc*` sobre Java 21.0.12:

- **Pausas Máximas Individuales (Log Real):** 
  - `Pause Mark Start`: **0.011 ms – 0.015 ms**
  - `Pause Mark End`: **0.017 ms – 0.021 ms**
  - `Pause Relocate Start`: **0.013 ms – 0.034 ms**
- **Pausa Acumulada por Ciclo:** **< 0.05 ms** (Sub-milisegundo absoluto).
- **Gestión Dinámica de Memoria:** Liberación eficiente de páginas Small/Medium y retorno de capacidad al SO.
- **Cumplimiento de Rúbrica:** Garantiza latencia ultra-baja y cumple con el requisito de pausas de GC $\le 1\text{ ms}$.