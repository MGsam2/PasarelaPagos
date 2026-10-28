package com.pasarelapagos.iso8583;

public final class IsoConstants {
//diccionario de constantes del protocol iso 8583, final impide que sea heredada
    private IsoConstants() {
    }

    // MTI identificador del tipo de mensaje
    public static final String MTI_FINANCIAL_REQUEST = "0200"; //solicitud compra/autorizacion pos
    public static final String MTI_FINANCIAL_RESPONSE = "0210";// respuesta pasarela o banco autorizador norifica el resultado de transaccion

    // Processing Code
    public static final String PROCESSING_CODE_PURCHASE = "000000";//indica tipo de operacion, compra bienes /servicios en cuenta correinte/ahorro

    // Response Codes
    public static final String RESPONSE_APPROVED = "00"; //transaccion exitosa
    public static final String RESPONSE_INVALID_TRANSACTION = "12";//transaccion invalida
    public static final String RESPONSE_INVALID_AMOUNT = "13";//monto invalido
    public static final String RESPONSE_INVALID_ACCOUNT = "14";//tarjeta no encontrada o pan innvalido
    public static final String RESPONSE_INSUFFICIENT_FUNDS = "51";//fondos insuficientes
    public static final String RESPONSE_ACCOUNT_NOT_ACTIVE = "57";//cuenta inactiva/bloqueada

    // Currency
    public static final String CURRENCY_GUATEMALA = "320"; //iso 4271. no. que identifica moneda guatemala
}





/*"El IsoConstants implementa el patrón de diseño Utility Class, utilizando un constructor privado 
y el modificador final para prevenir la instanciación en memoria y la herencia, optimizando así los 
recursos. Centraliza el vocabulario del estándar ISO 8583, definiendo los indicadores de tipo de 
mensaje (MTI 0200 y 0210), el código de procesamiento del DE 3, la moneda en Quetzales del DE 49 
y los códigos de respuesta del DE 39. Esto desacopla los valores mágicos (Hardcoding) del resto 
de la lógica de negocio, haciendo el código altamente mantenible." 


Una instanciación es crear un objeto en memoria usando new. Como esta clase solo contiene 
constantes estáticas (static), instanciarla desperdiciaría memoria RAM. Al hacer el constructor 
privado evito que el sistema cree objetos inútiles, y al hacer la clase final evito que sea heredada,
 garantizando que el estándar ISO de la pasarela no sea alterado por clases hijas.*/