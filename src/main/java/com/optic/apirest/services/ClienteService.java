package com.optic.apirest.services;


import com.optic.apirest.dto.cliente.ClienteRequest;
import com.optic.apirest.dto.cliente.ClienteResponse;
import com.optic.apirest.dto.cliente.mappers.ClienteMapper;
import com.optic.apirest.models.Cliente;
import com.optic.apirest.respositories.ClienteRepository;
import com.optic.apirest.services.interfaces.IClienteService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 🎯 PRINCIPIOS SOLID APLICADOS:
 * 
 * ✅ SRP (Single Responsibility): Solo maneja lógica de clientes
 * ✅ DIP (Dependency Inversion): Implementa IClienteService, depende de abstracciones
 * ✅ OCP (Open/Closed): Abierto para extensión, cerrado para modificación
 */
@Service
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper ;

    // Inyección por constructor (mejor práctica)
    public ClienteService(ClienteRepository clienteRepository , ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        Cliente cliente = clienteMapper.toEntity(request);

        // Verificar si el cliente ya existe por documentoIdentidad
        Cliente clienteExistente = clienteRepository.findClienteByDocumentoIdentidad(cliente.getDocumentoIdentidad());
        if (clienteExistente != null) {
            // Si el cliente ya existe, devolver su información
            return clienteMapper.toResponse(clienteExistente);
        }
        // Si el cliente no existe, guardarlo y devolver su información
        Cliente nuevoCliente = clienteRepository.save(cliente);
        return  clienteMapper.toResponse(nuevoCliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return clienteMapper.toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> findAll(String query) {
    List<Cliente> clientes;

        if (query != null && !query.isBlank()) {
            clientes = clienteRepository.findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(query, query);
        } else {
            clientes = clienteRepository.findAll();
        }

        return clientes.stream()//stream es para trabajar con colecciones de datos nos permite aplicar operaciones funcionales como map, filter, reduce, etc.
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList()); //esto es para convertir la lista de entidades a lista de responses nos sirve para evitar codigo repetitivo
    }
    @Transactional
    public void update(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        clienteMapper.updateEntity(cliente,  request);
        clienteRepository.save(cliente);
    }
    @Transactional
    public void delete(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(cliente);
    }

}
