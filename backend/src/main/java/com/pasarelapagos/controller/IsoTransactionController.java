package com.pasarelapagos.controller;

import com.pasarelapagos.iso8583.IsoTransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iso8583")
public class IsoTransactionController {

    private final IsoTransactionService isoTransactionService;

    public IsoTransactionController(
            IsoTransactionService isoTransactionService) {

        this.isoTransactionService = isoTransactionService;
    }

    @PostMapping(
            value = "/transacciones",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> procesarTransaccion(
            @RequestBody byte[] mensajeIso) throws Exception {

        byte[] respuesta =
                isoTransactionService.procesar(mensajeIso);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(respuesta);
    }
}