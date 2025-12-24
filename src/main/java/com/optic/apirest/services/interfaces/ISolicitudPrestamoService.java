package com.optic.apirest.services.interfaces;

import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoRequest;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoResponse;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoUpdate;

import java.util.List;

/**
 * 🎯 PRINCIPIO SOLID APLICADO: Interface Segregation (ISP) + Dependency Inversion (DIP)
 * 
 * Esta interfaz define el contrato para operaciones con solicitudes de préstamo.
 * 
 * ✅ VENTAJAS:
 * - Facilita testing con mocks
 * - Permite cambiar implementación sin afectar consumidores
 * - Código más desacoplado y mantenible
 */
public interface ISolicitudPrestamoService {
    
    /**
     * Crea una nueva solicitud de préstamo.
     */
    SolicitudPrestamoResponse create(SolicitudPrestamoRequest request);
    
    /**
     * Simula una solicitud sin guardarla en la base de datos.
     */
    SolicitudPrestamoResponse simulador(SolicitudPrestamoRequest request);
    
    /**
     * Busca una solicitud por su ID.
     */
    SolicitudPrestamoResponse findById(Long id);
    
    /**
     * Busca todas las solicitudes, opcionalmente filtradas por query.
     */
    List<SolicitudPrestamoResponse> findAll(String query);
    
    /**
     * Actualiza una solicitud existente.
     */
    SolicitudPrestamoResponse update(Long solicitudId, SolicitudPrestamoUpdate request);
    
    /**
     * Elimina una solicitud por su ID.
     */
    void delete(Long id);
}
