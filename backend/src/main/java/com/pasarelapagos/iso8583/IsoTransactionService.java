package com.pasarelapagos.iso8583;

import com.pasarelapagos.corebancario.CoreBancarioClient;
import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.ruteador.Ruteador;
import com.pasarelapagos.ruteador.RuteadorPagoResponse;
import com.pasarelapagos.service.IdempotenciaService;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;

@Service
public class IsoTransactionService {

    private final IsoParser isoParser;
    private final IsoToPagoMapper isoToPagoMapper;
    private final Ruteador ruteador;
    private final IsoResponseMapper isoResponseMapper;
    private final IdempotenciaService idempotenciaService;
    private final CoreBancarioClient coreBancarioClient;

    public IsoTransactionService(
            IsoParser isoParser,
            IsoToPagoMapper isoToPagoMapper,
            Ruteador ruteador,
            IsoResponseMapper isoResponseMapper,
            IdempotenciaService idempotenciaService,
            CoreBancarioClient coreBancarioClient) {

        this.isoParser = isoParser;
        this.isoToPagoMapper = isoToPagoMapper;
        this.ruteador = ruteador;
        this.isoResponseMapper = isoResponseMapper;
        this.idempotenciaService = idempotenciaService;
        this.coreBancarioClient = coreBancarioClient;
    }

    public byte[] procesar(byte[] mensajeIso) throws Exception {

        ISOMsg solicitud = isoParser.unpack(mensajeIso);

        try {
            PagoRequest pagoRequest = isoToPagoMapper.convertir(solicitud);

            // Generar clave de idempotencia usando RRN o combinación PAN + STAN
            String claveIdempotencia = (pagoRequest.rrn() != null && !pagoRequest.rrn().isBlank())
                    ? pagoRequest.rrn()
                    : pagoRequest.pan() + "_" + pagoRequest.stan();

            int codigoRespuesta;

            // 1. Verificación de Idempotencia
            if (idempotenciaService.esTransaccionDuplicada(claveIdempotencia)) {
                codigoRespuesta = idempotenciaService.obtenerRespuestaPrevia(claveIdempotencia);
            } else {
                // 2. Procesamiento transaccional
                codigoRespuesta = ruteador.procesarPago(
                        pagoRequest.pan(),
                        pagoRequest.monto(),
                        pagoRequest.stan()
                );

                // Registrar en caché de idempotencia
                idempotenciaService.registrarTransaccion(claveIdempotencia, codigoRespuesta);
            }

            String codigoIso = String.format("%02d", codigoRespuesta);

            ISOMsg respuesta = isoResponseMapper.crearRespuestaDesdeCodigo(
                    solicitud,
                    codigoIso
            );

            return isoParser.pack(respuesta);

        } catch (Exception exception) {

            ISOMsg respuesta = isoResponseMapper.crearRespuestaError(
                    solicitud,
                    IsoConstants.RESPONSE_INVALID_ACCOUNT
            );

            return isoParser.pack(respuesta);
        }
    }
}