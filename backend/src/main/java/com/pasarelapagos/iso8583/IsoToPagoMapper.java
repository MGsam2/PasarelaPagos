package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.entity.Cuenta;
import com.pasarelapagos.exception.RecursoNoEncontradoException;
import com.pasarelapagos.repository.CuentaRepository;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IsoToPagoMapper {

    private final CuentaRepository cuentaRepository;

    public IsoToPagoMapper(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public PagoRequest convertir(ISOMsg mensaje) throws Exception {

        validarMensaje(mensaje);

        String pan = mensaje.getString(2);
        String montoIso = mensaje.getString(4);
        String stan = mensaje.getString(11);

        BigDecimal monto = convertirMonto(montoIso);

        /*
         * Buscamos la cuenta real utilizando
         * el PAN recibido en DE2.
         */
        Cuenta cuenta = cuentaRepository.findByPan(pan)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe una cuenta asociada al PAN"
                        )
                );

        PagoRequest request = new PagoRequest();

        request.setCuentaId(cuenta.getId());
        request.setMonto(monto);
        request.setStan(stan);

        return request;
    }

    private void validarMensaje(ISOMsg mensaje) throws Exception {

        if (mensaje == null) {
            throw new IllegalArgumentException(
                    "El mensaje ISO 8583 no puede ser null"
            );
        }

        if (!IsoConstants.MTI_FINANCIAL_REQUEST
                .equals(mensaje.getMTI())) {

            throw new IllegalArgumentException(
                    "MTI no válido. Se esperaba "
                            + IsoConstants.MTI_FINANCIAL_REQUEST
            );
        }

        if (!mensaje.hasField(2)) {
            throw new IllegalArgumentException(
                    "Falta DE2 - PAN"
            );
        }

        if (!mensaje.hasField(4)) {
            throw new IllegalArgumentException(
                    "Falta DE4 - monto"
            );
        }

        if (!mensaje.hasField(11)) {
            throw new IllegalArgumentException(
                    "Falta DE11 - STAN"
            );
        }
    }

    private BigDecimal convertirMonto(String montoIso) {

        if (montoIso == null || montoIso.isBlank()) {
            throw new IllegalArgumentException(
                    "El monto ISO 8583 está vacío"
            );
        }

        long centavos = Long.parseLong(montoIso);

        return BigDecimal
                .valueOf(centavos)
                .movePointLeft(2);
    }
}