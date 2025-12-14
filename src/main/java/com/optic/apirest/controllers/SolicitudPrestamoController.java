package com.optic.apirest.controllers;

import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoRequest;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoResponse;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoUpdate;
import com.optic.apirest.services.SolicitudPrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de Solicitudes de Préstamo con protección de permisos
 * 
 * Permisos requeridos:
 * - SIMULATE_LOANS: Para simular préstamos
 * - CREATE_LOANS: Para crear solicitudes de préstamo
 * - READ_LOANS: Para listar y ver solicitudes
 * - UPDATE_LOANS: Para actualizar solicitudes
 * - DELETE_LOANS: Para eliminar solicitudes
 */
@RestController
@RequestMapping("/api/solicitudesPrestamo")
public class SolicitudPrestamoController {

    private final SolicitudPrestamoService solicitudPrestamoService;

    // Inyección por constructor (mejor práctica)
    public SolicitudPrestamoController(SolicitudPrestamoService solicitudPrestamoService) {
        this.solicitudPrestamoService = solicitudPrestamoService;
    }

    @PostMapping("/simular")
    public ResponseEntity<?> simulador(@Valid  @RequestBody SolicitudPrestamoRequest request) {
        try {
            SolicitudPrestamoResponse solicitudPrestamo =   solicitudPrestamoService.simulador(request);
            return ResponseEntity.status(200).body(solicitudPrestamo);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(),
                            "statusCode", HttpStatus.BAD_REQUEST.value()
                    ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> create(@Valid  @RequestBody SolicitudPrestamoRequest request) {
        try {
            SolicitudPrestamoResponse solicitudPrestamo =   solicitudPrestamoService.create(request);
            return ResponseEntity.status(201).body(solicitudPrestamo);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(),
                            "statusCode", HttpStatus.BAD_REQUEST.value()
                    ));
        }
    }
    @PreAuthorize("hasAuthority('READ_LOANS')")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        try {
            SolicitudPrestamoResponse response = solicitudPrestamoService.findById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(),
                            "statusCode", HttpStatus.NOT_FOUND.value()
                    ));
        }
    }

    @PreAuthorize("hasAuthority('READ_LOANS')")
    @GetMapping()
    public ResponseEntity<List<SolicitudPrestamoResponse>>  findAll(@RequestParam(required = false) String query) {
        List<SolicitudPrestamoResponse> response = solicitudPrestamoService.findAll(query);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('UPDATE_LOANS')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SolicitudPrestamoUpdate request) {
        try {
            SolicitudPrestamoResponse response = solicitudPrestamoService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(),
                            "statusCode", HttpStatus.NOT_FOUND.value()
                    ));
        }
    }
    @PreAuthorize("hasAuthority('DELETE_LOANS')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id){
        solicitudPrestamoService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Solicitud de préstamo eliminada exitosamente" , "codigo", "202"));//map.off crea un mapa inmutable con una sola entrada es decir una clave y un valor
    }



}
