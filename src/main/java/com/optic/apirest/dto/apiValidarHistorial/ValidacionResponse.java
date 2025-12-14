package com.optic.apirest.dto.apiValidarHistorial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionResponse {
    private String dni;
    private Integer riesgo;
    private String resultadoValidacion;
}
