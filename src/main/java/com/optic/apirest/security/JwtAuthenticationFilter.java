package com.optic.apirest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT - Intercepta TODAS las peticiones HTTP para validar el token JWT
 * 
 * Este filtro se ejecuta ANTES de cualquier controlador
 * 
 * FLUJO:
 * 1. Cliente envía request con header: Authorization: Bearer <token>
 * 2. Este filtro extrae el token del header
 * 3. Valida el token con JwtService
 * 4. Si es válido, carga el usuario y lo autentica en Spring Security
 * 5. La petición continúa hacia el controlador
 * 
 * OncePerRequestFilter: Se ejecuta UNA VEZ por cada request
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Método principal del filtro
     * 
     * Se ejecuta automáticamente en cada request HTTP
     * 
     * @param request petición HTTP entrante
     * @param response respuesta HTTP saliente
     * @param filterChain cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(// sirve para filtrar las peticiones http
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extraer el header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Verificar si el header existe y empieza con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No hay token, continuar sin autenticar
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (remover "Bearer " del inicio)
        jwt = authHeader.substring(7);
        
        try {
            // 4. Extraer el username del token
            username = jwtService.extractUsername(jwt);

            // 5. Si hay username y el usuario NO está autenticado aún
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 6. Cargar los detalles del usuario desde la BD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 7. Validar el token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // 8. Crear token de autenticación para Spring Security es decir lo que se hace es crear un token de autenticacion con los detalles del usuario y sus autoridades
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // 9. Agregar detalles adicionales del request
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // 10. Establecer autenticación en el contexto de seguridad
                    // Ahora Spring Security sabe que este usuario está autenticado
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si hay error al procesar el token, continuar sin autenticar
            // El endpoint protegido rechazará el acceso
            logger.error("Error al procesar JWT: " + e.getMessage());
        }

        // 11. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
