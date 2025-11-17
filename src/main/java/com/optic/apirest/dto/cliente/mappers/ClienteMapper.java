package com.optic.apirest.dto.cliente.mappers;

import com.optic.apirest.dto.cliente.ClienteRequest;
import com.optic.apirest.dto.cliente.ClienteResponse;
import com.optic.apirest.models.Cliente;
import org.springframework.stereotype.Component;

@Component//sirve para que spring lo detecte como un bean es decir un componente gestionado por el contenedor de spring
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente();//esto sirve para crear una nueva instancia de la clase Cliente, la instancia es un objeto que representa un cliente en el sistema
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setIngresoMensual(request.getIngresoMensual());
        cliente.setRegEstado(1);// Por defecto activo
        return cliente;
    }

    public ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();//esto sirve para crear una nueva instancia de la clase ClienteResponse, la instancia es un objeto que representa la respuesta del cliente en el sistema
        response.setId(cliente.getId());
        response.setNombreCompleto(cliente.getNombreCompleto());
        response.setDocumentoIdentidad(cliente.getDocumentoIdentidad());
        response.setEmail(cliente.getEmail());
        response.setTelefono(cliente.getTelefono());
        response.setIngresoMensual(cliente.getIngresoMensual());
        return response;
    }

    public Cliente updateEntity(Cliente cliente, ClienteRequest request) {
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setIngresoMensual(request.getIngresoMensual());
        cliente.setRegEstado (2); // Por defecto activo
        return cliente;
    }



}
