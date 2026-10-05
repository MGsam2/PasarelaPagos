package com.pasarelapagos.service;

import com.pasarelapagos.entity.Cliente;
import com.pasarelapagos.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import com.pasarelapagos.exception.RecursoNoEncontradoException;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente crearCliente(String nombre, String documento) {

        Cliente cliente = new Cliente(nombre, documento);

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {

        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {

        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cliente no encontrado"
                        ));
    }
}