package com.pasarelapagos.iso8583;

import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.dto.PagoResponse;
import com.pasarelapagos.entity.Transaccion;
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
    public byte[] procesar(byte[] mensajeIso) throws Exception {

        /*
         * 1. Recibir y desempaquetar el mensaje ISO 8583.
         */
        ISOMsg solicitud = isoParser.unpack(mensajeIso);

        /*
         * 2. Convertir ISO 8583 -> PagoRequest.
         */
        PagoRequest pagoRequest =
                isoToPagoMapper.convertir(solicitud);

        /*
         * 3. Ejecutar la lógica de negocio.
         */
        Transaccion transaccion =
                pagoService.realizarPago(
                        pagoRequest.getCuentaId(),
                        pagoRequest.getMonto(),
                        pagoRequest.getStan()
                );

        /*
         * 4. Convertir la Transaccion a PagoResponse.
         */
        PagoResponse pagoResponse =
                new PagoResponse(transaccion);

        /*
         * 5. Convertir PagoResponse -> ISO 0210.
         */
        ISOMsg respuesta =
                isoResponseMapper.convertir(
                        pagoResponse,
                        solicitud
                );

        /*
         * 6. Empaquetar ISO 0210 para devolverlo
         *    al sistema que originó la transacción.
         */
        return isoParser.pack(respuesta);
    }
}