package com.pasarelapagos.controller;

import com.pasarelapagos.entity.Cliente;
import com.pasarelapagos.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*CONTROLADOR REST DE CLIENTES:
 Mapea las solicitudes HTTP relacionadas con los titulares de las cuentas
 e interactúa con ClienteService*/
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    //Inyeccion de dependencias por constructor
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /*Recibe los parámetros en el formulario/URL (@RequestParam) y crea un nuevo tarjetahabiente.*/
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

    /*retorna el catalogo completo de clientes guardados en PSQL */
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {

        return ResponseEntity.ok(
                clienteService.listarClientes()
        );
    }

    /*Extrae la variable de la ruta @PathVariable y retorna los datos del cliente */
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarCliente(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.buscarPorId(id)
        );
    }
}
/*El ClienteController expone los endpoints REST bajo /api/clientes. 
Permite crear, listar y consultar clientes vía HTTP GET y POST, 
delegando el procesamiento al ClienteService y devolviendo 
respuestas JSON estandarizadas en ResponseEntity. */