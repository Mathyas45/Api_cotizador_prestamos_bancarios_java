package com.optic.apirest.services.interfaces;

import com.optic.apirest.dto.cliente.ClienteRequest;
import com.optic.apirest.dto.cliente.ClienteResponse;

import java.util.List;

/**
 * 🎯 PRINCIPIO SOLID APLICADO: Interface Segregation (ISP) + Dependency Inversion (DIP)
 * 
 * Esta interfaz define el contrato para operaciones con clientes.
 * 
 * ✅ VENTAJAS:
 * - Facilita testing con mocks
 * - Permite cambiar implementación sin afectar consumidores
 * - Código más desacoplado y mantenible
 */
public interface IClienteService {
    
    /**
     * Crea un nuevo cliente o retorna el existente si ya existe.
     */
    ClienteResponse create(ClienteRequest request);
    
    /**
     * Busca un cliente por su ID.
     */
    ClienteResponse findById(Long id);
    
    /**
     * Busca todos los clientes, opcionalmente filtrados por query.
     */
    List<ClienteResponse> findAll(String query);
    
    /**
     * Actualiza un cliente existente.
     */
    void update(Long id, ClienteRequest request);
    
    /**
     * Elimina un cliente por su ID.
     */
    void delete(Long id);
}
