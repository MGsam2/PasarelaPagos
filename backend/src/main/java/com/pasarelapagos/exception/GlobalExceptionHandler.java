package com.pasarelapagos.exception;

import com.pasarelapagos.iso8583.IsoConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

/*MANEJADOR GLOBAL DE EXCEPCIONES:
 Intercepta los errores lanzados en la capa de controladores/servicios 
 y devuelve respuestas JSON estandarizadas.*/
@RestControllerAdvice
public class GlobalExceptionHandler {

     /*INTERCEPTOR DE RECURSOS NO ENCONTRADOS:
     Captura cuando se busca un cliente, cuenta o tarjeta inexistente.*/
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException exception) {

        //Construye un mapa inmutable con al estructura estandar de error
        Map<String, Object> respuesta = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 404,
                "error", "NOT_FOUND",
                "message", exception.getMessage(),
                //Mapea el error HTTP al codigo financiero ISO 8583 correspodiente
                "codigoRespuesta",
                IsoConstants.RESPONSE_INVALID_ACCOUNT
        );

        //Retorna HTTP 404 Not Found con el cuerpo JSON
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(respuesta);
    }
}
/*El GlobalExceptionHandler utiliza @RestControllerAdvice para capturar excepciones no controladas de manera centralizada. 
Al detectar un RecursoNoEncontradoException, devuelve un JSON con código HTTP 404 estandarizado y el código de rechazo ISO 8583 '14'*/