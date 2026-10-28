package com.pasarelapagos.controller;

import com.pasarelapagos.iso8583.IsoTransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*Expone un endpoint REST especializado en procesar tramas financieras ISO 
 enviadas como arreglos de bytes crudos (byte[])*/
@RestController
@RequestMapping("/api/iso8583")
public class IsoTransactionController {

    private final IsoTransactionService isoTransactionService;

    public IsoTransactionController(
            IsoTransactionService isoTransactionService) {

        this.isoTransactionService = isoTransactionService;
    }

    /*PROCESAR TRANSACCIÓN BINARIA (HTTP POST /api/iso8583/transacciones):
     @param consumes = APPLICATION_OCTET_STREAM_VALUE: Indica que la petición recibe bytes puros.
     @param produces = APPLICATION_OCTET_STREAM_VALUE: Indica que la respuesta es un arreglo de bytes*/
    @PostMapping(
            value = "/transacciones",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> procesarTransaccion(
            @RequestBody byte[] mensajeIso) throws Exception {
                //1. Delega el procesamiento completo al servicio orquestador TCP/ISO
        byte[] respuesta =
                isoTransactionService.procesar(mensajeIso);
                //2. Retorna la trama de respuesta empaquetada en bytes con codigo HTTP 200 OK
        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(respuesta);
    }
}
/*El IsoTransactionController expone el endpoint /api/iso8583/transacciones 
en formato octet-stream (bytes crudos). Permite probar el pipeline completo 
de autorización ISO 8583 a través de peticiones HTTP REST sin pasar por el socket TCP */