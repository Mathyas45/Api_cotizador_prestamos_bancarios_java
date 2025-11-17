package com.optic.apirest.dto.cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // Genera automáticamente getters, setters, toString, equals y hashCode
@NoArgsConstructor //esto es para que el constructor por defecto sea visible
@AllArgsConstructor // esto es para que el constructor con todos los parametros sea visible es decir que todos los atributos sean visibles
// esto es para que el constructor con todos los parametros sea visible es decir que todos los atributos sean visibles
public class ClienteResponse {

    public long id;
    public String nombreCompleto;
    public String documentoIdentidad;
    public String email;
    public String telefono;
    public BigDecimal ingresoMensual;
}
