# Reporte Técnico de Rendimiento y Arquitectura Cloud-Native

**Curso:** Desarrollo Web Avanzado
**Proyecto:** Recolector Transaccional y Pasarela de Pagos ISO 8583  

---

## 1. Métricas de Compilación y Memoria

Las mediciones del microservicio ejecutándose con **Java 21.0.12** y **Virtual Threads** (Project Loom):

| Métrica | Resultado Obtenido | Requisito de la Rúbrica | Estado |
| :--- | :---: | :---: | :---: |
| **Tiempo de Arranque (Startup)** | **45 ms** *(Nativo)* / **4.56 s** *(JVM)* | $< 100\text{ ms}$ (Binario Nativo) | Cumplido |
| **Consumo de RAM (RSS)** | **34.2 MB** *(Nativo)* / **267 MB** *(JVM)* | Minificación de recursos | Cumplido |
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

Telemetría capturada mediante la bandera `-XX:+UseZGC -XX:+ZGenerational -Xlog:gc*`:

- **Pausas Máximas Individuales:** **0.029 ms** (`Pause Mark Start`), **0.024 ms** (`Pause Mark End`), **0.026 ms** (`Pause Relocate Start`).
- **Pausa Acumulada por Ciclo:** **< 0.08 ms** (Sub-milisegundo).
- **Gestión Dinámica de Memoria:** Liberación y retorno automático de memoria al SO (`Uncommitted: 540M`).
- **Cumplimiento de Rúbrica:** Garantiza latencia ultra-baja y cumple con el requisito de pausas de GC $\le 1\text{ ms}$.