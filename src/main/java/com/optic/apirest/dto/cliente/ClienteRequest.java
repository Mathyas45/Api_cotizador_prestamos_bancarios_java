package com.optic.apirest.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data //esto genera getters y setters automáticamente
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    public String nombreCompleto;

    @NotBlank(message = "El documento de identidad es obligatorio")
    @Size(max = 20, message = "El documento de identidad no puede exceder 20 caracteres")
    public String documentoIdentidad;

    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    public String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, max = 15, message = "El teléfono debe tener entre 9 y 15 caracteres")
    public String telefono;

    @Size(max = 10, message = "El ingreso mensual no puede exceder 10 dígitos")
    public BigDecimal ingresoMensual;

}
