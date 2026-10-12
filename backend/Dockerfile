# ==========================================
# ETAPA 1: Compilación AOT con GraalVM Native Image
# ==========================================
FROM ghcr.io/graalvm/native-image-community:21 AS builder

WORKDIR /app

# Copiar archivos de dependencias Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar binario nativo AOT
RUN ./mvnw -Pnative native:compile -DskipTests

# ==========================================
# ETAPA 2: Imagen Final Distroless (Minificada & Non-Root)
# ==========================================
FROM gcr.io/distroless/cc-debian12:nonroot

WORKDIR /app

# Copiar el binario nativo generado en la etapa de compilación
COPY --from=builder /app/target/pasarela-pagos /app/pasarela-pagos

# Puertos expuestos: 8080 (REST/HTTP) y 9090 (TCP Sockets ISO)
EXPOSE 8080 9090

# Ejecución del binario nativo con ZGC de Generación (Generational ZGC)
ENTRYPOINT ["/app/pasarela-pagos", "-XX:+UseZGC", "-XX:+ZGenerational"]