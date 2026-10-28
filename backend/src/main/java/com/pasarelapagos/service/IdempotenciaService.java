package com.pasarelapagos.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Idempotencia de ultra-alta velocidad para evitar transacciones duplicadas.
 * Protege contra reintentos de red del terminal POS y débitos dobles.
 * Implementa control de capacidad máxima (Bounded Cache) y TTL para prevenir fugas de memoria.
 */
@Service
public class IdempotenciaService {

    // Capacidad máxima de transacciones en caché simultáneas
    private static final int CAPACIDAD_MAXIMA = 200_000;
    // TTL por defecto: 10 minutos en milisegundos
    private static final long TTL_MILLIS = 10L * 60 * 1000;

    /**
     * Registro inmutable que almacena la respuesta y su estampa de tiempo.
     */
    public record RegistroTransaccion(int codigoRespuesta, long timestamp) {}

    private final Map<String, RegistroTransaccion> transaccionesProcesadas = new ConcurrentHashMap<>();

    /**
     * Verifica si una clave única (RRN o PAN_STAN) ya fue procesada y se encuentra vigente.
     */
    public boolean esTransaccionDuplicada(String claveUnica) {
        if (claveUnica == null) {
            return false;
        }
        RegistroTransaccion registro = transaccionesProcesadas.get(claveUnica);
        if (registro == null) {
            return false;
        }
        // Validar expiración por TTL
        if (System.currentTimeMillis() - registro.timestamp() > TTL_MILLIS) {
            transaccionesProcesadas.remove(claveUnica, registro);
            return false;
        }
        return true;
    }

    /**
     * Recupera el código de respuesta original otorgado a la transacción.
     */
    public Integer obtenerRespuestaPrevia(String claveUnica) {
        if (claveUnica == null) {
            return null;
        }
        RegistroTransaccion registro = transaccionesProcesadas.get(claveUnica);
        return registro != null ? registro.codigoRespuesta() : null;
    }

    /**
     * Registra el resultado de una transacción en memoria protegida contra sobreflujo.
     */
    public void registrarTransaccion(String claveUnica, int codigoRespuesta) {
        if (claveUnica == null) {
            return;
        }
        // Evicción de seguridad si se alcanza el umbral de capacidad
        if (transaccionesProcesadas.size() >= CAPACIDAD_MAXIMA) {
            eviccionSeguridad();
        }
        transaccionesProcesadas.put(
                claveUnica,
                new RegistroTransaccion(codigoRespuesta, System.currentTimeMillis())
        );
    }

    /**
     * Limpieza controlada de registros expirados para asegurar estabilidad de RAM.
     */
    private synchronized void eviccionSeguridad() {
        if (transaccionesProcesadas.size() >= CAPACIDAD_MAXIMA) {
            long ahora = System.currentTimeMillis();
            transaccionesProcesadas.entrySet().removeIf(
                    entry -> (ahora - entry.getValue().timestamp()) > TTL_MILLIS
            );
            // Si la tasa de transacciones aún supera el límite, limpiar los más antiguos
            if (transaccionesProcesadas.size() >= CAPACIDAD_MAXIMA) {
                transaccionesProcesadas.clear();
            }
        }
    }
}
