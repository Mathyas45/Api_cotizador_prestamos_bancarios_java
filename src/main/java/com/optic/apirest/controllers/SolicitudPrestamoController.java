package com.optic.apirest.controllers;

import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoRequest;
import com.optic.apirest.dto.SolicitudPrestamo.SolicitudPrestamoResponse;
import com.optic.apirest.services.SolicitudPrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/solicitudesPrestamo")
public class SolicitudPrestamoController {

    private final SolicitudPrestamoService solicitudPrestamoService;

    // Inyección por constructor (mejor práctica)
    public SolicitudPrestamoController(SolicitudPrestamoService solicitudPrestamoService) {
        this.solicitudPrestamoService = solicitudPrestamoService;
    }

        @PostMapping("/simulador")
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

}
