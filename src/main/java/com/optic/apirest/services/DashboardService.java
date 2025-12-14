package com.optic.apirest.services;

import com.optic.apirest.dto.dashboard.AprobadosRechazadosPorMesDTO;
import com.optic.apirest.dto.dashboard.DashboardResponse;
import com.optic.apirest.dto.dashboard.SolicitudesPorMesDTO;
import com.optic.apirest.respositories.ClienteRepository;
import com.optic.apirest.respositories.SolicitudPrestamoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final SolicitudPrestamoRepository solicitudRepository;

    public DashboardService(ClienteRepository clienteRepository,
                            SolicitudPrestamoRepository solicitudRepository) {
        this.clienteRepository = clienteRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public DashboardResponse getDashboardData() {

        long totalClientes = clienteRepository.count();//.count es un método proporcionado por Spring Data JPA que devuelve el número total de entidades en la tabla correspondiente al repositorio, entidades son las filas de la tabla de la base de datos.
        long totalSolicitudes = solicitudRepository.count();
        long aprobados = solicitudRepository.countByEstado(1);
        long rechazados = solicitudRepository.countByEstado(0);

        // Transformar solicitudesPorMes
        List<SolicitudesPorMesDTO> solicitudesPorMes = solicitudRepository.solicitudesPorMes()
                .stream()
                .map(obj -> new SolicitudesPorMesDTO((String) obj[0], ((Number) obj[1]).longValue()))
                .toList();

        // Transformar aprobadosRechazadosPorMes
        List<AprobadosRechazadosPorMesDTO> aprobadosRechazadosPorMes = solicitudRepository.aprobadosRechazadosPorMes()
                .stream()
                .map(obj -> new AprobadosRechazadosPorMesDTO(
                        (String) obj[0],
                        ((Number) obj[1]).longValue(),
                        ((Number) obj[2]).longValue()
                ))
                .toList();


        return new DashboardResponse(
                totalClientes,
                totalSolicitudes,
                aprobados,
                rechazados,
                solicitudesPorMes,
                aprobadosRechazadosPorMes

        );
    }
}