package com.optic.apirest.dto.SolicitudPrestamo;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitudPrestamoRequest {

    @NotNull
    private BigDecimal monto;
    @NotNull
    private BigDecimal porcentajeCuotaInicial;
    @NotNull
    private Integer plazoAnios;
    @NotNull
    private Long clienteId;
}

