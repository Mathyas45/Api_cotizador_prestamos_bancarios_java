package com.optic.apirest.services;

import com.optic.apirest.Client.TasaInteresApiClient;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoRequest;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoResponse;
import com.optic.apirest.dto.SolicitudPrestamo.mappers.SolicitudPrestamoMapper;
import com.optic.apirest.dto.apiValidarHistorial.ValidacionResponse;
import com.optic.apirest.models.SolicitudPrestamo;
import com.optic.apirest.respositories.ClienteRepository;
import com.optic.apirest.respositories.SolicitudPrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.apache.el.lang.ELArithmetic.divide;

@Service
public class SolicitudPrestamoService {

    private final SolicitudPrestamoRepository solicitudPrestamoRepository;
    private  final SolicitudPrestamoMapper solicitudPrestamoMapper;
    private  final TasaInteresApiClient tasaInteresApiClient;
    private  final ClienteRepository clienteRepository;

    // Inyección por constructor (mejor práctica)
    public SolicitudPrestamoService(SolicitudPrestamoRepository solicitudPrestamoRepository, SolicitudPrestamoMapper solicitudPrestamoMapper, TasaInteresApiClient tasaInteresApiClient, ClienteRepository clienteRepository) {
        this.solicitudPrestamoRepository = solicitudPrestamoRepository;
        this.solicitudPrestamoMapper = solicitudPrestamoMapper;
        this.tasaInteresApiClient = tasaInteresApiClient;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public SolicitudPrestamoResponse create(SolicitudPrestamoRequest request) {

        // 1️⃣ Buscar el cliente
        String documento = clienteRepository.findDocumentoIdentidadById(request.getClienteId());
        if (documento == null) {
            throw new RuntimeException("Cliente no encontrado");
        }

        // 2️⃣ Llamar a la API externa (MockAPI)
        ValidacionResponse validacion = tasaInteresApiClient.obtenerValidacionCliente(documento);

        // 3️⃣ Crear la solicitud base desde el mapper
        SolicitudPrestamo solicitud = solicitudPrestamoMapper.toEntity(request);


        // Riesgo y resultado de la API
        Double riesgo = validacion.getRiesgo();
        String resultado = validacion.getResultadoValidacion();

        // 4️⃣ Si no está aprobado → guardar rechazo y salir
        if (!resultado.equalsIgnoreCase("APROBADO")) {

            solicitud.setEstado(0); // RECHAZADO
            solicitud.setTasaInteres(BigDecimal.ZERO);
            solicitud.setTcea(BigDecimal.ZERO);
            solicitud.setMontoCuotaInicial(BigDecimal.ZERO);
            solicitud.setMontoFinanciar(BigDecimal.ZERO);
            solicitud.setCuotaMensual(BigDecimal.ZERO);
            solicitud.setMotivoRechazo("Solicitud rechazada por validación externa.");
            solicitud.setCliente(
                    clienteRepository.findById(request.getClienteId())
                            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"))
            );

            solicitudPrestamoRepository.save(solicitud);
            return solicitudPrestamoMapper.toResponse(solicitud);
        }

        // 5️⃣ Asignar tasa según riesgo, la operacion normal es: tasaInteresAnual =
        BigDecimal tasaInteresAnual =
                (riesgo == 1) ? BigDecimal.valueOf(7.5) :
                        (riesgo == 2) ? BigDecimal.valueOf(8.5) :
                                BigDecimal.valueOf(9.5);

        // 6️⃣ Cálculos financieros
        BigDecimal monto = request.getMonto(); // Monto solicitado
        BigDecimal porcentajeInicial = request.getPorcentajeCuotaInicial() // porcentaje inicial en decimal
                .divide(BigDecimal.valueOf(100));

        BigDecimal montoCuotaInicial = monto.multiply(porcentajeInicial); // Monto de la cuota inicial
        BigDecimal montoFinanciar = monto.subtract(montoCuotaInicial);// Monto a financiar

        int plazoMeses = request.getPlazoAnios() * 12; // Plazo en meses

        BigDecimal tasaMensual = tasaInteresAnual // Tasa de interés mensual
                .divide(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(12));

        // Fórmula francesa con BigDecimal correctamente implementada para calcular la cuota mensual: la formula normal para entenderlo seria :
        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoMasTasa.pow(plazoMeses);
        BigDecimal divisor = BigDecimal.ONE.divide(potencia, 20, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal cuotaMensual = montoFinanciar.multiply(tasaMensual)
                .divide(BigDecimal.ONE.subtract(divisor), 20, BigDecimal.ROUND_HALF_EVEN);

        BigDecimal tcea = unoMasTasa.pow(12) // TCEA anual es la tasa efectiva anual es decir la tasa que realmente se paga en un año considerando la capitalización de intereses
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        // 7️⃣ Asignar cálculos
        solicitud.setTasaInteres(tasaInteresAnual);
        solicitud.setTcea(tcea);
        solicitud.setMontoCuotaInicial(montoCuotaInicial);
        solicitud.setMontoFinanciar(montoFinanciar);
        solicitud.setCuotaMensual(cuotaMensual);
        solicitud.setEstado(1); // APROBADO
        solicitud.setMotivoRechazo(null);
        solicitud.setCliente(
                clienteRepository.findById(request.getClienteId())
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"))
        );

        // 8️⃣ Guardar
        SolicitudPrestamo  solicitudPrestamo  = solicitudPrestamoRepository.save(solicitud);

        return solicitudPrestamoMapper.toResponse(solicitudPrestamo);

    }
    @Transactional(readOnly = true)
    public SolicitudPrestamoResponse simulador(SolicitudPrestamoRequest request) {

        // 1️⃣ Buscar el cliente
        String documento = clienteRepository.findDocumentoIdentidadById(request.getClienteId());
        if (documento == null) {
            throw new RuntimeException("Cliente no encontrado");
        }
        // Lógica similar a la del método create, pero sin guardar en la base de datos

        // 1️⃣ Llamar a la API externa (MockAPI)
        ValidacionResponse validacion = tasaInteresApiClient.obtenerValidacionCliente(documento);

        // Riesgo y resultado de la API
        Double riesgo = validacion.getRiesgo();
        String resultado = validacion.getResultadoValidacion();

        // 2️⃣ Si no está aprobado → lanzar excepción
        if (!resultado.equalsIgnoreCase("APROBADO")) {
            throw new RuntimeException("Simulación no aprobada por validación externa.");
        }

        // 3️⃣ Asignar tasa según riesgo
        BigDecimal tasaInteresAnual =
                (riesgo == 1) ? BigDecimal.valueOf(7.5) :
                        (riesgo == 2) ? BigDecimal.valueOf(8.5) :
                                BigDecimal.valueOf(9.5);

        // 4️⃣ Cálculos financieros
        BigDecimal monto = request.getMonto();
        BigDecimal porcentajeInicial = request.getPorcentajeCuotaInicial()
                .divide(BigDecimal.valueOf(100));

        BigDecimal montoCuotaInicial = monto.multiply(porcentajeInicial);
        BigDecimal montoFinanciar = monto.subtract(montoCuotaInicial);

        int plazoMeses = request.getPlazoAnios() * 12;

        BigDecimal tasaMensual = tasaInteresAnual
                .divide(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(12));

        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoMasTasa.pow(plazoMeses);
        BigDecimal divisor = BigDecimal.ONE.divide(potencia, 20, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal cuotaMensual = montoFinanciar.multiply(tasaMensual)
                .divide(BigDecimal.ONE.subtract(divisor), 20, BigDecimal.ROUND_HALF_EVEN);

        BigDecimal tcea = unoMasTasa.pow(12)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        // 5️⃣ Crear respuesta simulada
        SolicitudPrestamoResponse response = new SolicitudPrestamoResponse();
        response.setMonto(monto);
        response.setPorcentajeCuotaInicial(request.getPorcentajeCuotaInicial());
        response.setMontoCuotaInicial(montoCuotaInicial);
        response.setMontoFinanciar(montoFinanciar);
        response.setPlazoAnios(request.getPlazoAnios());
        response.setTasaInteres(tasaInteresAnual);
        response.setTcea(tcea);
        response.setCuotaMensual(cuotaMensual);
        return response;
    }


}