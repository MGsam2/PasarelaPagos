package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.dto.PagoResponse;
import com.pasarelapagos.entity.Transaccion;
import com.pasarelapagos.exception.RecursoNoEncontradoException;
import com.pasarelapagos.service.PagoService;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IsoTransactionService {

    private final IsoParser isoParser;
    private final IsoToPagoMapper isoToPagoMapper;
    private final PagoService pagoService;
    private final IsoResponseMapper isoResponseMapper;

    public IsoTransactionService(
            IsoParser isoParser,
            IsoToPagoMapper isoToPagoMapper,
            PagoService pagoService,
            IsoResponseMapper isoResponseMapper) {

        this.isoParser = isoParser;
        this.isoToPagoMapper = isoToPagoMapper;
        this.pagoService = pagoService;
        this.isoResponseMapper = isoResponseMapper;
    }

    @Transactional
    public byte[] procesar(byte[] mensajeIso)
            throws Exception {

        ISOMsg solicitud =
                isoParser.unpack(mensajeIso);

        try {

            PagoRequest pagoRequest =
                    isoToPagoMapper.convertir(solicitud);

            Transaccion transaccion =
                    pagoService.realizarPago(
                            pagoRequest.getCuentaId(),
                            pagoRequest.getMonto(),
                            pagoRequest.getStan()
                    );

            PagoResponse pagoResponse =
                    new PagoResponse(transaccion);

            ISOMsg respuesta =
                    isoResponseMapper.convertir(
                            pagoResponse,
                            solicitud
                    );

            return isoParser.pack(respuesta);

        } catch (RecursoNoEncontradoException exception) {

            ISOMsg respuesta =
                    isoResponseMapper.crearRespuestaError(
                            solicitud,
                            IsoConstants.RESPONSE_INVALID_ACCOUNT
                    );

            return isoParser.pack(respuesta);
        }
    }
}