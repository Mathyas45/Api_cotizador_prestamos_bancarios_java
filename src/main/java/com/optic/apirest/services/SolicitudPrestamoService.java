package com.optic.apirest.services;

import com.optic.apirest.Client.TasaInteresApiClient;
import com.optic.apirest.config.TasaInteresConfig;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoRequest;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoResponse;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoUpdate;
import com.optic.apirest.dto.SolicitudPrestamo.mappers.SolicitudPrestamoMapper;
import com.optic.apirest.dto.apiValidarHistorial.ValidacionResponse;
import com.optic.apirest.models.SolicitudPrestamo;
import com.optic.apirest.respositories.ClienteRepository;
import com.optic.apirest.respositories.SolicitudPrestamoRepository;
import com.optic.apirest.services.interfaces.ISolicitudPrestamoService;
import com.optic.apirest.utils.CalculadoraFinanciera;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 🎯 PRINCIPIOS SOLID APLICADOS:
 * 
 * ✅ SRP (Single Responsibility): Solo maneja lógica de solicitudes de préstamo
 * ✅ DIP (Dependency Inversion): Implementa ISolicitudPrestamoService, depende de abstracciones
 * ✅ OCP (Open/Closed): Usa TasaInteresConfig para configuración (extensible sin modificar)
 */
@Service
public class SolicitudPrestamoService implements ISolicitudPrestamoService {

    private final SolicitudPrestamoRepository solicitudPrestamoRepository;
    private final SolicitudPrestamoMapper solicitudPrestamoMapper;
    private final TasaInteresApiClient tasaInteresApiClient;
    private final ClienteRepository clienteRepository;
    private final TasaInteresConfig tasaInteresConfig; // 🎯 SOLID: Configuración centralizada

    // Inyección por constructor (mejor práctica - Dependency Injection)
    public SolicitudPrestamoService(
            SolicitudPrestamoRepository solicitudPrestamoRepository,
            SolicitudPrestamoMapper solicitudPrestamoMapper,
            TasaInteresApiClient tasaInteresApiClient,
            ClienteRepository clienteRepository,
            TasaInteresConfig tasaInteresConfig) {
        this.solicitudPrestamoRepository = solicitudPrestamoRepository;
        this.solicitudPrestamoMapper = solicitudPrestamoMapper;
        this.tasaInteresApiClient = tasaInteresApiClient;
        this.clienteRepository = clienteRepository;
        this.tasaInteresConfig = tasaInteresConfig;
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
        Integer riesgo = validacion.getRiesgo();
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

        // 5️⃣ Obtener tasa según riesgo desde configuración centralizada (SOLID: SRP)
        BigDecimal tasaInteresAnual = tasaInteresConfig.obtenerTasaPorRiesgo(riesgo);

        // 6️⃣ Cálculos financieros usando CalculadoraFinanciera (SOLID: SRP - código reutilizable)
        CalculadoraFinanciera.ResultadoCalculo calculo = CalculadoraFinanciera.calcularTodo(
                request.getMonto(),
                request.getPorcentajeCuotaInicial(),
                request.getPlazoAnios(),
                tasaInteresAnual
        );

        BigDecimal montoCuotaInicial = calculo.getMontoCuotaInicial();
        BigDecimal montoFinanciar = calculo.getMontoFinanciar();
        BigDecimal cuotaMensual = calculo.getCuotaMensual();
        BigDecimal tcea = calculo.getTcea();

        // 7️⃣ Asignar cálculos
        solicitud.setTasaInteres(tasaInteresAnual);
        solicitud.setTcea(tcea);
        solicitud.setMontoCuotaInicial(montoCuotaInicial);
        solicitud.setMontoFinanciar(montoFinanciar);
        solicitud.setCuotaMensual(cuotaMensual);
        solicitud.setEstado(1); // APROBADO
        solicitud.setMotivoRechazo(null);
        solicitud.setRiesgoCliente(BigDecimal.valueOf(riesgo).intValue());
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
        System.out.println("Simulador request: " + request);
        // 1️⃣ Buscar el cliente
        String documento = clienteRepository.findDocumentoIdentidadById(request.getClienteId());
        if (documento == null) {
            throw new RuntimeException("Cliente no encontrado");
        }
        // Lógica similar a la del método create, pero sin guardar en la base de datos

        // 1️⃣ Llamar a la API externa (MockAPI)
        ValidacionResponse validacion = tasaInteresApiClient.obtenerValidacionCliente(documento);

        // Riesgo y resultado de la API
        Integer riesgo = validacion.getRiesgo();
        String resultado = validacion.getResultadoValidacion();

        SolicitudPrestamoResponse response = new SolicitudPrestamoResponse();
        // 3️⃣ Crear la solicitud base desde el mapper
        SolicitudPrestamo solicitud = solicitudPrestamoMapper.toEntity(request);
        System.out.println("Simulador solicitud base: " + solicitud);
        // 2️⃣ Si no está aprobado → lanzar excepción
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

            return solicitudPrestamoMapper.toResponseCotizacion(solicitud);

        }

        // 3️⃣ Obtener tasa según riesgo desde configuración centralizada (SOLID: SRP)
        BigDecimal tasaInteresAnual = tasaInteresConfig.obtenerTasaPorRiesgo(riesgo);

        // 4️⃣ Cálculos financieros usando CalculadoraFinanciera (SOLID: código reutilizable)
        CalculadoraFinanciera.ResultadoCalculo calculo = CalculadoraFinanciera.calcularTodo(
                request.getMonto(),
                request.getPorcentajeCuotaInicial(),
                request.getPlazoAnios(),
                tasaInteresAnual
        );

        BigDecimal monto = request.getMonto();
        BigDecimal montoCuotaInicial = calculo.getMontoCuotaInicial();
        BigDecimal montoFinanciar = calculo.getMontoFinanciar();
        BigDecimal cuotaMensual = calculo.getCuotaMensual();
        BigDecimal tcea = calculo.getTcea();

        // 5️⃣ Crear respuesta simulada
        solicitud.setMonto(monto);
        solicitud.setPorcentajeCuotaInicial(request.getPorcentajeCuotaInicial());
        solicitud.setMontoCuotaInicial(montoCuotaInicial);
        solicitud.setMontoFinanciar(montoFinanciar);
        solicitud.setPlazoAnios(request.getPlazoAnios());
        solicitud.setTasaInteres(tasaInteresAnual);
        solicitud.setTcea(tcea);
        solicitud.setEstado(1);  // APROBADO
        solicitud.setCuotaMensual(cuotaMensual);
        solicitud.setCliente(
                clienteRepository.findById(request.getClienteId())
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado"))
        );

        return solicitudPrestamoMapper.toResponseCotizacion(solicitud);
    }

    @Transactional(readOnly = true)
    public SolicitudPrestamoResponse findById(Long id){
        SolicitudPrestamo solicitud = solicitudPrestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud de Préstamo no encontrada con ID: " + id));
        return solicitudPrestamoMapper.toResponse(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudPrestamoResponse> findAll(String query) {
        List<SolicitudPrestamo> solicitudes;

        if (query != null && !query.isBlank()) {
            solicitudes = solicitudPrestamoRepository
                    .buscarPorClienteIdNombreODocumento(query, query);
        } else {
            solicitudes = solicitudPrestamoRepository.findAll();
        }

        return solicitudes.stream()
                .map(solicitudPrestamoMapper::toResponse)
                .toList();
    }

    @Transactional
    public SolicitudPrestamoResponse update(Long solicitudId, SolicitudPrestamoUpdate request) {

        // 1️⃣ Buscar la solicitud en la BD
        SolicitudPrestamo solicitud = solicitudPrestamoRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // 2️⃣ Usar el riesgo YA guardado (no llamar API)
        Integer riesgo = solicitud.getRiesgoCliente();

        // 3️⃣ Obtener tasa según riesgo desde configuración centralizada (SOLID: SRP)
        BigDecimal tasaInteresAnual = tasaInteresConfig.obtenerTasaPorRiesgo(riesgo);

        // 4️⃣ Actualizar campos modificables
        solicitud.setMonto(request.getMonto());
        solicitud.setPlazoAnios(request.getPlazoAnios());
        solicitud.setPorcentajeCuotaInicial(request.getPorcentajeCuotaInicial());

        // 5️⃣ Recalcular valores financieros usando CalculadoraFinanciera (SOLID: código reutilizable)
        CalculadoraFinanciera.ResultadoCalculo calculo = CalculadoraFinanciera.calcularTodo(
                request.getMonto(),
                request.getPorcentajeCuotaInicial(),
                request.getPlazoAnios(),
                tasaInteresAnual
        );

        BigDecimal montoCuotaInicial = calculo.getMontoCuotaInicial();
        BigDecimal montoFinanciar = calculo.getMontoFinanciar();
        BigDecimal cuotaMensual = calculo.getCuotaMensual();
        BigDecimal tcea = calculo.getTcea();

        // 6️⃣ Guardar recalculos
        solicitud.setTasaInteres(tasaInteresAnual);
        solicitud.setTcea(tcea);
        solicitud.setMontoCuotaInicial(montoCuotaInicial);
        solicitud.setMontoFinanciar(montoFinanciar);
        solicitud.setCuotaMensual(cuotaMensual);

        // 7️⃣ Guardar cambios
        SolicitudPrestamo actualizada = solicitudPrestamoRepository.save(solicitud);

        // 8️⃣ Devolver response
        return solicitudPrestamoMapper.toResponse(actualizada);
    }

    @Transactional
    public void delete(Long id) {
        SolicitudPrestamo solicitud = solicitudPrestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud de Préstamo no encontrada con ID: " + id));
        solicitudPrestamoRepository.delete(solicitud);

    }
}