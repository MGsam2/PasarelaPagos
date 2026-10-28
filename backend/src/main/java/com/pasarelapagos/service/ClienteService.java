package com.pasarelapagos.service;

import com.pasarelapagos.entity.Cliente;
import com.pasarelapagos.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import com.pasarelapagos.exception.RecursoNoEncontradoException;
import java.util.List;

/*SERVICIO DE GESTION DE CLIENTE
Encapsula las reglas de negocio para crear, listar y consultar los tarjetahabientes */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    //inteccion de dependencias por constructor de Spring
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }
    //Crear cliente, instacia la entidad Cliente y la persiste en PostgreSQL
    public Cliente crearCliente(String nombre, String documento) {

        Cliente cliente = new Cliente(nombre, documento);

        return clienteRepository.save(cliente);
    }
    //listar clientes: Retorna el catalogo completo de clientes registrados
    public List<Cliente> listarClientes() {

        return clienteRepository.findAll();
    }
    //Buscar por ID: COnsulta un cliente por su ID Si el Optioal viene Vacio, lanza RecursoNoEncontradoException,
    //evitando NullPinterException
    public Cliente buscarPorId(Long id) {

        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cliente no encontrado"
                        ));
    }
}
/*"El ClienteService gestiona la administración de tarjetahabientes. 
Utiliza ClienteRepository para ejecutar operaciones de persistencia e integración con PostgreSQL, 
aplicando el patrón Optional para manejar de forma segura las consultas de clientes inexistentes
 mediante RecursoNoEncontradoException." */