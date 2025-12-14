package com.optic.apirest.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AprobadosRechazadosPorMesDTO {
    private String mes;
    private Long aprobados;
    private Long rechazados;
}