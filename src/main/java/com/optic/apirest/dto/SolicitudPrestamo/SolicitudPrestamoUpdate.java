package com.optic.apirest.dto.SolicitudPrestamo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitudPrestamoUpdate {
    private BigDecimal monto;
    private Integer plazoAnios;
    private BigDecimal porcentajeCuotaInicial;
}
