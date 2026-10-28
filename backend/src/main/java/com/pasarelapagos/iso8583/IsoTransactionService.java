package com.pasarelapagos.iso8583;

import com.pasarelapagos.corebancario.CoreBancarioClient;
import com.pasarelapagos.dto.PagoRequest;
import com.pasarelapagos.ruteador.Ruteador;
import com.pasarelapagos.ruteador.RuteadorPagoResponse;
import com.pasarelapagos.service.IdempotenciaService;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;

/*SERVICIO PIPELINE TCP ORQUESTADOR
coordina el flujo completo desde que entrada la rafaga de bytes por el socket TCP
hasta que se devuelva el mensaje de autorizado iso 0210 */
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

    /*Pipeline de procesamiento tcp
    recibe bytes de la red tcp puerto 9090 y retorna bytes formateados en iso 8583 */
    public byte[] procesar(byte[] mensajeIso) throws Exception {

        //1. desempaquetar- convierte los bytes de red en un objeto ISOMsg
        ISOMsg solicitud = isoParser.unpack(mensajeIso);

        try {
            //2. MAPEAR: extrae el dto de negocio (PagoRequest) desde los campos ISO (DE 2, DE 4, DE 11)
            PagoRequest pagoRequest = isoToPagoMapper.convertir(solicitud);

            //3.Generar clave de idempotencia usando RRN o combinación PAN + STAN - huella unica
            String claveIdempotencia = (pagoRequest.rrn() != null && !pagoRequest.rrn().isBlank())
                    ? pagoRequest.rrn()
                    : pagoRequest.pan() + "_" + pagoRequest.stan();

            int codigoRespuesta;

            //4. Verificación de Idempotencia: evita cobros dobles si la terminal reenvia la peticion
            if (idempotenciaService.esTransaccionDuplicada(claveIdempotencia)) {
                //recupera la respuesta previa de memoria sin tocar la BD
                codigoRespuesta = idempotenciaService.obtenerRespuestaPrevia(claveIdempotencia);
            } else {
                // 5. Procesamiento transaccional, enrutar y debitar, envia al ruteador para  conmutar a Banco A o Banco B
                codigoRespuesta = ruteador.procesarPago(
                        pagoRequest.pan(),
                        pagoRequest.monto(),
                        pagoRequest.stan()
                );

                //Guarda el resultado en caché de idempotencia
                idempotenciaService.registrarTransaccion(claveIdempotencia, codigoRespuesta);
            }
            //formatea el codigo de respuesta numerico a 2 digitos texto ( 0 -> 00)
            String codigoIso = String.format("%02d", codigoRespuesta);
            //6. Mapear salida: construye el objeto ISOMsg de respuesta (MTI 0210)
            ISOMsg respuesta = isoResponseMapper.crearRespuestaDesdeCodigo(
                    solicitud,
                    codigoIso
            );
            //7. Empaquetar: convierte la respuesta 0210 a bytes para enviar por TCP
            return isoParser.pack(respuesta);

        } catch (Exception exception) {
            //Manejo de excepciones Devuelve respuesta ISO de error (Codigo 14) en formato de bytes
            ISOMsg respuesta = isoResponseMapper.crearRespuestaError(
                    solicitud,
                    IsoConstants.RESPONSE_INVALID_ACCOUNT
            );

            return isoParser.pack(respuesta);
        }
    }
}
/*"El IsoTransactionService es el orquestador principal del Socket TCP. Recibe la trama en bytes del puerto 9090, 
la desempaca con IsoParser, verifica idempotencia con IdempotenciaService para evitar cobros dobles, 
conmuta la transacción al banco correspondiente con Ruteador y devuelve la trama empaquetada de respuesta 0210 en milisegundos." */

/*Recibe la trama en bytes desde el puerto TCP 9090.

Llama al IsoParser para desarmar la trama (unpack).

Llama al IsoToPagoMapper para extraer el objeto PagoRequest.

Verifica si es una transacción duplicada usando IdempotenciaService:

Si es duplicada: Reutiliza el resultado previo sin tocar el banco.

Si es nueva: Delega la transacción al Ruteador 
para cobrar en Banco A o Banco B y guarda el resultado.

Construye la respuesta ISO 0210 con IsoResponseMapper 
y la empaca en bytes con IsoParser para enviarla de vuelta por la red. */