package com.optic.apirest.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private long totalClientes;
    private long totalSolicitudes;
    private long totalAprobados;
    private long totalRechazados;
    private List<SolicitudesPorMesDTO> solicitudesPorMes;
    private List<AprobadosRechazadosPorMesDTO> aprobadosRechazadosPorMes;
}
