package com.pasarelapagos.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotenciaService {

    // Caché en memoria thread-safe para rastrear transacciones procesadas (RRN/STAN)
    private final Map<String, Integer> transaccionesProcesadas = new ConcurrentHashMap<>();

    public boolean esTransaccionDuplicada(String claveUnica) {
        return transaccionesProcesadas.containsKey(claveUnica);
    }

    public Integer obtenerRespuestaPrevia(String claveUnica) {
        return transaccionesProcesadas.get(claveUnica);
    }

    public void registrarTransaccion(String claveUnica, int codigoRespuesta) {
        transaccionesProcesadas.put(claveUnica, codigoRespuesta);
    }
}