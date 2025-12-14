package com.optic.apirest.config;

import com.optic.apirest.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de Spring Security
 * 
 * IMPORTANTE: Esta clase tiene DOS configuraciones:
 * 
 * 1. MODO DESARROLLO/PRUEBAS (securityFilterChainDev):
 *    - Todo abierto, sin JWT, sin protección
 *    - Para probar rápidamente con Postman
 *    - DESCOMENTAR para desarrollo
 * 
 * 2. MODO PRODUCCIÓN (securityFilterChainProd):
 *    - JWT habilitado
 *    - CORS configurado para Angular
 *    - Rutas protegidas
 *    - COMENTAR para desarrollo
 * 
 * ⚠️ SOLO UNO PUEDE ESTAR ACTIVO A LA VEZ
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    // ╔══════════════════════════════════════════════════════════════════════╗
    // ║                    🔓 MODO DESARROLLO / PRUEBAS                       ║
    // ║         Descomentar esta sección para desarrollo con Postman          ║
    // ║              Comentar la sección de PRODUCCIÓN abajo                  ║
    // ╚══════════════════════════════════════════════════════════════════════╝
    
    /*
    @Bean
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ⚠️ TODO ABIERTO - Solo para desarrollo
                );
        return http.build();
    }
    */

    // ╔══════════════════════════════════════════════════════════════════════╗
    // ║                      🔒 MODO PRODUCCIÓN                               ║
    // ║         Esta es la configuración segura con JWT y CORS               ║
    // ║              Comentar para desarrollo con Postman                     ║
    // ╚══════════════════════════════════════════════════════════════════════╝
    
    @Bean
    public SecurityFilterChain securityFilterChainProd(HttpSecurity http) throws Exception {
        http
                // 1. Habilitar CORS para Angular
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 2. Deshabilitar CSRF (no necesario con JWT)
                .csrf(csrf -> csrf.disable())
                
                // 3. Configurar autorización de rutas
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (sin autenticación)
                        .requestMatchers(
                                "/api/auth/**",           // Login y registro
                                "/api/clientes/register", // Crear cliente
                                "/api/solicitudesPrestamo/simular",
                                "/api/solicitudesPrestamo/register", // Crear solicitud de préstamo
                                "/api/public/**",         // Endpoints públicos
                                "/swagger-ui/**",         // Documentación Swagger
                                "/v3/api-docs/**",        // OpenAPI docs
                                "/actuator/health"        // Health check
                        ).permitAll()
                        
                        // Rutas de admin (requieren rol ADMIN)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Todas las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                )
                
                // 4. Sin sesiones (stateless - cada request debe tener JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // 5. Usar nuestro AuthenticationProvider
                .authenticationProvider(authenticationProvider())
                
                // 6. Agregar filtro JWT antes del filtro de autenticación
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ CONFIGURACIÓN DE CORS
     * Permite que Angular (localhost:4200) haga peticiones al backend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (Angular en desarrollo y producción)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",      // Angular dev
                "http://localhost:3000",      // Otros frontends
                //para kotlin 10.0.2.2
                "http://10.0.2.2:4200",       // Emulador Android (acceso a frontend en host)
                "http://10.0.2.2:8080",       // Emulador Android (acceso a backend en host)
                "https://tudominio.com"       // Producción (cambiar por tu dominio)
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        // Exponer el header Authorization para que el frontend pueda leerlo
        configuration.setExposedHeaders(List.of("Authorization"));

        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Tiempo máximo de cache de preflight requests (1 hora)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica CORS a TODAS las rutas

        return source;
    }

    /**
     * Proveedor de autenticación
     * Conecta UserDetailsService con PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Encriptador de contraseñas BCrypt
     * Usado para hashear y verificar contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
