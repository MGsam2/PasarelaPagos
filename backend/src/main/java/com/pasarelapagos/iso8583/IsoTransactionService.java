package com.pasarelapagos.iso8583;

import com.pasarelapagos.corebancario.CoreBancarioClient;
import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.ruteador.Ruteador;
import com.pasarelapagos.ruteador.RuteadorPagoResponse;
import com.pasarelapagos.service.IdempotenciaService;
import com.pasarelapagos.util.MaskingUtils;
import org.jpos.iso.ISOMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Orquestador principal del pipeline transaccional ISO 8583.
 * Coordina el ciclo de vida completo: desempaque, verificación de idempotencia,
 * enrutamiento o consumo HTTP mediante RestClient y empaque de respuesta MTI 0210.
 */
@Service
public class IsoTransactionService {

    private static final Logger log = LoggerFactory.getLogger(IsoTransactionService.class);

    private final IsoParser isoParser;
    private final IsoToPagoMapper isoToPagoMapper;
    private final Ruteador ruteador;
    private final IsoResponseMapper isoResponseMapper;
    private final IdempotenciaService idempotenciaService;
    private final CoreBancarioClient coreBancarioClient;

    @Value("${corebancario.adapter.mode:direct}")
    private String coreBancarioAdapterMode;

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

    /**
     * Pipeline transaccional TCP no bloqueante asistido por Virtual Threads.
     * Recibe los bytes crudos del socket y retorna la trama empaquetada ISO 0210.
     */
    public byte[] procesar(byte[] mensajeIso) throws Exception {
        ISOMsg solicitud;
        try {
            // 1. Desempaquetar: convierte los bytes de red en un objeto ISOMsg
            solicitud = isoParser.unpack(mensajeIso);
        } catch (Exception e) {
            log.error("Error al desempaquetar trama ISO 8583: {}", e.getMessage());
            throw e;
        }

        try {
            // 2. Mapear: extrae DTO inmutable con validación de Data Elements (DE 2, 3, 4, 11, 37, 102)
            PagoRequest pagoRequest = isoToPagoMapper.convertir(solicitud);

            // 3. Generar clave de idempotencia única (RRN o PAN + STAN)
            String claveIdempotencia = (pagoRequest.rrn() != null && !pagoRequest.rrn().isBlank())
                    ? pagoRequest.rrn()
                    : pagoRequest.pan() + "_" + pagoRequest.stan();

            int codigoRespuesta;

            // 4. Verificación de Idempotencia: evita cobros duplicados en reintentos de red
            if (idempotenciaService.esTransaccionDuplicada(claveIdempotencia)) {
                log.info("Transacción duplicada detectada para clave {}. Retornando respuesta previa.", claveIdempotencia);
                codigoRespuesta = idempotenciaService.obtenerRespuestaPrevia(claveIdempotencia);
            } else {
                // 5. Procesamiento transaccional (adaptador HTTP RestClient o directo según configuración)
                if ("http".equalsIgnoreCase(coreBancarioAdapterMode)) {
                    log.debug("Enrutando transacción vía RestClient hacia Core Bancario HTTP");
                    RuteadorPagoResponse respuestaHttp = coreBancarioClient.procesarPagoCore(pagoRequest);
                    codigoRespuesta = respuestaHttp.codigoRespuesta();
                } else {
                    codigoRespuesta = ruteador.procesarPago(
                            pagoRequest.pan(),
                            pagoRequest.monto(),
                            pagoRequest.stan()
                    );
                }

                // Almacenar en caché de idempotencia
                idempotenciaService.registrarTransaccion(claveIdempotencia, codigoRespuesta);
            }

            // 6. Formatear código de respuesta a 2 caracteres estándar
            String codigoIso = String.format("%02d", codigoRespuesta);

            // 7. Mapear salida: construye ISOMsg 0210 con mirroring exacto
            ISOMsg respuesta = isoResponseMapper.crearRespuestaDesdeCodigo(solicitud, codigoIso);

            log.debug("Transacción completada para PAN {} con código DE 39: {}",
                    MaskingUtils.maskPan(pagoRequest.pan()), codigoIso);

            // 8. Empaquetar a bytes para responder por el socket TCP
            return isoParser.pack(respuesta);

        } catch (IllegalArgumentException ex) {
            log.warn("Solicitud ISO inválida: {}", ex.getMessage());
            String codigoError = (ex.getMessage() != null && ex.getMessage().contains("PAN"))
                    ? IsoConstants.RESPONSE_INVALID_ACCOUNT
                    : IsoConstants.RESPONSE_FORMAT_ERROR;

            return responderConError(solicitud, codigoError);

        } catch (Exception exception) {
            log.error("Fallo inesperado del sistema al procesar trama ISO: {}", exception.getMessage(), exception);
            return responderConError(solicitud, IsoConstants.RESPONSE_SYSTEM_ERROR);
        }
    }

    private byte[] responderConError(ISOMsg solicitud, String codigoError) throws Exception {
        ISOMsg respuesta = isoResponseMapper.crearRespuestaDesdeCodigo(solicitud, codigoError);
        return isoParser.pack(respuesta);
    }
}
