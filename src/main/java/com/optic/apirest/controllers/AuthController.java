package com.optic.apirest.controllers;

import com.optic.apirest.dto.ApiResponse;
import com.optic.apirest.dto.auth.AuthResponse;
import com.optic.apirest.dto.auth.LoginRequest;
import com.optic.apirest.dto.auth.RegisterRequest;
import com.optic.apirest.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Autenticación
 * 
 * Endpoints:
 * - POST /api/auth/register: Registrar nuevo usuario
 * - POST /api/auth/login: Iniciar sesión con EMAIL
 * 
 * Estas rutas son PÚBLICAS (no requieren token JWT)
 * configurado en SecurityConfig
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registrar nuevo usuario
     * 
     * POST /api/auth/register
     * 
     * Body:
     * {
     *   "username": "juan123",
     *   "password": "mipassword123",
     *   "email": "juan@example.com"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse authResponse = authService.register(request);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(true)
                            .message("Usuario registrado exitosamente")
                            .data(authResponse)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message(e.getMessage() != null ? e.getMessage() : "Error durante el registro")
                            .build());
        }
    }

    /**
     * Iniciar sesión con EMAIL
     * 
     * POST /api/auth/login
     * 
     * Body:
     * {
     *   "email": "juan@example.com",
     *   "password": "mipassword123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            
            return ResponseEntity
                    .ok(ApiResponse.<AuthResponse>builder()
                            .success(true)
                            .message("Login exitoso")
                            .data(authResponse)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<AuthResponse>builder()
                            .success(false)
                            .message(e.getMessage() != null ? e.getMessage() : "Credenciales incorrectas")
                            .build());
        }
    }
}
