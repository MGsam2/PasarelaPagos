package com.pasarelapagos.controller;

import com.pasarelapagos.entity.Cliente;
import com.pasarelapagos.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(
            @RequestParam String nombre,
            @RequestParam String documento) {

        Cliente cliente =
                clienteService.crearCliente(
                        nombre,
                        documento
                );

        return ResponseEntity.ok(cliente);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {

        return ResponseEntity.ok(
                clienteService.listarClientes()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarCliente(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.buscarPorId(id)
        );
    }
}