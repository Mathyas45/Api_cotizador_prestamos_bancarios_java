package com.optic.apirest.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolicitudesPorMesDTO {
    private String mes;
    private Long total;
}