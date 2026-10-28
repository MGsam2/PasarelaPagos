# ==============================================================================
# Multi-stage build (GraalVM 25 + Distroless nonroot)
# Proyecto: Recolector Transaccional ISO 8583 - Cloud Native
# ==============================================================================

# ETAPA 1: Compilación AOT con GraalVM Native Image 25
FROM ghcr.io/graalvm/native-image-community:25 AS builder

WORKDIR /app

# Copiar configuración Maven Wrapper
COPY backend/.mvn/ .mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente del backend
COPY backend/src ./src

# Compilación AOT nativa con soporte para ZGC
RUN ./mvnw -Pnative native:compile -DskipTests

# ETAPA 2: Imagen Final Distroless (Minificada y Non-Root)
FROM gcr.io/distroless/cc-debian12:nonroot

WORKDIR /app

# Copiar el binario nativo generado en la etapa de compilación
COPY --from=builder /app/target/pasarela-pagos /app/pasarela-pagos

# Puertos expuestos: 8080 (REST/HTTP) y 9090 (TCP Sockets ISO 8583)
EXPOSE 8080 9090

# Ejecución del binario nativo con Generational ZGC
ENTRYPOINT ["/app/pasarela-pagos", "-XX:+UseZGC", "-XX:+ZGenerational"]

