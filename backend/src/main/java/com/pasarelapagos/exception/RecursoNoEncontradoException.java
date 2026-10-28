package com.pasarelapagos.exception;

/*EXCEPCIÓN DE RECURSO NO ENCONTRADO:
 Excepción personalizada no chequeada (extiende de RuntimeException)
 que se dispara cuando un cliente, cuenta o tarjeta no existe en PostgreSQL*/
public class RecursoNoEncontradoException extends RuntimeException {

    /*CONSTRUCTOR: Recibe el mensaje descriptivo del error (ej: "Cuenta no encontrada").
     Pasa el texto a la clase base RuntimeException*/
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

/*La clase RecursoNoEncontradoException es una excepción de dominio no chequeada. 
Se lanza desde la capa de servicios cuando una búsqueda por ID o PAN no devuelve resultados, 
desencadenando la respuesta de error 404 estandarizada en la API*/