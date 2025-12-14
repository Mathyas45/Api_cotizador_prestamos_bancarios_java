package com.optic.apirest.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO de respuesta después de login exitoso
 * 
 * Contiene:
 * - token: JWT token para autenticación en requests futuros
 * - type: Tipo de token (siempre "Bearer")
 * - username: Nombre del usuario autenticado
 * - email: Email del usuario
 * - roles: Lista de roles del usuario
 * - permissions: Lista de permisos del usuario (para el frontend)
 * 
 * El frontend guarda este token y lo envía en el header Authorization
 * de cada request: "Authorization: Bearer <token>"
 * 
 * Los permisos se usan en el frontend para:
 * - Mostrar/ocultar botones (ej: botón eliminar solo si tiene DELETE_CLIENTS)
 * - Habilitar/deshabilitar menús
 * - Controlar acceso a rutas/páginas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private String username;
    private String email;
    private Set<String> roles;
    
    /**
     * Lista de permisos del usuario
     * Ejemplos: ["READ_CLIENTS", "CREATE_CLIENTS", "DELETE_CLIENTS", ...]
     * 
     * El frontend usa esto para control de UI:
     * if (permissions.includes('DELETE_CLIENTS')) { mostrarBotonEliminar(); }
     */
    private Set<String> permissions;
}
