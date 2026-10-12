# Pasarela de Pagos

Proyecto final del curso de **Desarrollo Web**.

Este sistema implementa un conmutador/recolector financiero *cloud-native*, capaz de recibir solicitudes mediante **Sockets TCP/IP (ISO 8583)** y **API REST**. Procesa las solicitudes de manera no bloqueante y las enruta hacia cores bancarios aislados.

## Tecnologías utilizadas

- **Java 21+** y **Spring Boot 4.1** (Project Loom / Virtual Threads).
- **Jakarta EE 11** y **Spring Data JPA**.
- **Spring Integration IP** (servidor de sockets TCP).
- **RestClient** (Spring Framework 7).
- **GraalVM Native Image** y **Distroless OCI Images**.
- **PostgreSQL** (bases de datos independientes: `pasarela_pagos` y `banco_b`).
- **React 19**, **Vite** y **Tailwind CSS v4** (frontend).

## Estructura del proyecto

```text
PasarelaPagos/
├── backend/
│   ├── Dockerfile      # Multi-stage build (GraalVM + Distroless)
│   ├── src/            # Sockets TCP, parser ISO 8583, RestClient y ruteador
│   └── pom.xml
├── frontend/
│   ├── src/            # Interfaz de usuario de adquirencia
│   └── package.json
├── README.md           # Guía de arquitectura y puesta en marcha
└── REPORT.md           # Reporte técnico de rendimiento y ZGC
```

## Guía de ejecución

### 1. Prerrequisitos

Antes de ejecutar el proyecto, asegúrate de tener instalados y configurados los siguientes componentes:

- Java 21 o superior.
- PostgreSQL en ejecución en `localhost:5432`.
- Node.js y npm.
- Maven Wrapper incluido en el proyecto.

**Configuración de PostgreSQL**

Se asume que PostgreSQL utiliza el usuario `postgres` y la contraseña `123`.

Crea las dos bases de datos:

```sql
CREATE DATABASE pasarela_pagos;
CREATE DATABASE banco_b;
```

### 2. Iniciar el backend (Spring Boot)

Abre una terminal en la carpeta `backend` y ejecuta el comando correspondiente a tu sistema operativo.

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

Una vez iniciado el backend, los servicios estarán disponibles en las siguientes direcciones:

- **Servidor HTTP REST:** [http://localhost:8080](http://localhost:8080)
- **Servidor de sockets TCP (ISO 8583):** `localhost:9090`

### 3. Iniciar el frontend (React + Vite)

Abre otra terminal en la carpeta `frontend` y ejecuta:

```bash
npm install
npm run dev
```

La aplicación web estará disponible en:

[http://localhost:5173](http://localhost:5173)

## Tarjetas de prueba (BINs)

Utiliza las siguientes tarjetas de prueba para verificar el enrutamiento y el procesamiento de las transacciones.

| Número de tarjeta (PAN) | Banco destino | Comportamiento esperado |
|---|---|---|
| `4111 1111 1111 1111` | Banco A | Transacción aprobada (`00`). |
| `5222 2222 2222 2222` | Banco B | Transacción aprobada (`00`). |
| `4111 1111 1111 0000` | Inexistente | Transacción rechazada: tarjeta no encontrada (`14`). |

## Pruebas automatizadas del backend

Para ejecutar la suite completa de pruebas unitarias y de integración, incluidas las pruebas con WireMock y sockets TCP, ejecuta el siguiente comando desde la carpeta `backend`.

**Windows (PowerShell):**

```powershell
.\mvnw.cmd clean test
```

Se esperan **17 pruebas exitosas de 17** (`17/17 tests`), según los resultados previstos del proyecto.
