package com.optic.apirest.dto.SolicitudPrestamo;

import com.optic.apirest.dto.cliente.ClienteResponse;
import com.optic.apirest.dto.cliente.mappers.ClienteMapper;
import com.optic.apirest.models.Cliente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data // Genera automáticamente getters, setters, toString, equals y hashCode
@NoArgsConstructor //esto es para que el constructor por defecto sea visible
@AllArgsConstructor// esto es para que el constructor con todos los parametros sea visible es decir que todos los atributos sean visibles
public class SolicitudPrestamoResponse {
    public long id;
    public BigDecimal monto;
    public BigDecimal montoCuotaInicial;
    public BigDecimal porcentajeCuotaInicial;
    public BigDecimal montoFinanciar;
    public int plazoAnios;
    public BigDecimal tasaInteres;
    public BigDecimal tcea;
    public BigDecimal cuotaMensual;
    public String motivoRechazo;
    public Integer estado;
    private ClienteResponse cliente;

}
