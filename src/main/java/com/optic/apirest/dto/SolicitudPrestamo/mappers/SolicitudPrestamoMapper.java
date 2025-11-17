package com.optic.apirest.dto.SolicitudPrestamo.mappers;

import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoRequest;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoResponse;
import com.optic.apirest.dto.cliente.mappers.ClienteMapper;
import com.optic.apirest.models.SolicitudPrestamo;
import org.springframework.stereotype.Component;

@Component//sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class SolicitudPrestamoMapper {

    private final ClienteMapper clienteMapper;
    public SolicitudPrestamoMapper(ClienteMapper clienteMapper) {
        this.clienteMapper = clienteMapper;
    }

    public SolicitudPrestamo toEntity(SolicitudPrestamoRequest request) {
        SolicitudPrestamo solicitud = new SolicitudPrestamo();//esto sirve para crear una nueva instancia de la clase SolicitudPrestamo, la instancia es un objeto que representa una solicitud de prestamo en el sistema
        solicitud.setMonto(request.getMonto());
        solicitud.setPlazoAnios(request.getPlazoAnios());
        solicitud.setPorcentajeCuotaInicial(request.getPorcentajeCuotaInicial());
        return solicitud;
    }

    public SolicitudPrestamoResponse toResponse(SolicitudPrestamo solicitud) {
        SolicitudPrestamoResponse response = new SolicitudPrestamoResponse();//esto sirve para crear una nueva instancia de la clase SolicitudPrestamoResponse, la instancia es un objeto que representa la respuesta de la solicitud de prestamo en el sistema
        response.setId(solicitud.getId());
        if (solicitud.getCliente() != null) {
            response.setCliente(clienteMapper.toResponse(solicitud.getCliente()));
        }
        response.setMonto(solicitud.getMonto());
        response.setPlazoAnios(solicitud.getPlazoAnios());
        response.setPorcentajeCuotaInicial(solicitud.getPorcentajeCuotaInicial());
        response.setTasaInteres(solicitud.getTasaInteres());
        response.setTcea(solicitud.getTcea());
        response.setMontoCuotaInicial(solicitud.getMontoCuotaInicial());
        response.setMontoFinanciar(solicitud.getMontoFinanciar());
        response.setCuotaMensual(solicitud.getCuotaMensual());
        response.setEstado(solicitud.getEstado());
        response.setMotivoRechazo(solicitud.getMotivoRechazo());
        response.setCliente(clienteMapper.toResponse(solicitud.getCliente()));
        return response;
    }
}
