package com.pasarelapagos.service;

import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/*servicio para proporcionar metodos de lectura para consultar las transaccioens aprobadas y rechazadas */
@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    public TransaccionService(
            TransaccionRepository transaccionRepository) {

        this.transaccionRepository = transaccionRepository;
    }
    //retorna todas las transacciones registradas en la pasarela
    public List<Transaccion> listarTransacciones() {

        return transaccionRepository.findAll();
    }
    // retorna las transacciones pertenecientes a una cuenta especifica
    //ordenada cronologicamente de forma descendente, ultimos movimientos primero
    public List<Transaccion> listarPorCuenta(
            Long cuentaId) {

        return transaccionRepository
                .findByCuentaIdOrderByFechaHoraDesc(cuentaId);
    }
}
/*"El TransaccionService gestiona la lectura del historial transaccional. 
Utiliza TransaccionRepository para alimentar los reportes globales y la vista de estado de cuenta en el frontend, 
ordenando las operaciones de manera descendente por fecha y hora." */