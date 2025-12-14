package com.optic.apirest.services;

import com.optic.apirest.dto.auth.AuthResponse;
import com.optic.apirest.dto.auth.LoginRequest;
import com.optic.apirest.dto.auth.RegisterRequest;
import com.optic.apirest.models.Permission;
import com.optic.apirest.models.Role;
import com.optic.apirest.models.User;
import com.optic.apirest.respositories.RoleRepository;
import com.optic.apirest.respositories.UserRepository;
import com.optic.apirest.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de Autenticación - Maneja login y registro de usuarios
 * 
 * RESPONSABILIDADES:
 * - Registrar nuevos usuarios
 * - Autenticar usuarios existentes
 * - Generar tokens JWT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registra un nuevo usuario en el sistema
     * 
     * FLUJO:
     * 1. Verifica que username y email no existan
     * 2. Encripta la contraseña con BCrypt
     * 3. Asigna el rol USER por defecto
     * 4. Guarda el usuario en la BD
     * 5. Genera un token JWT
     * 6. Retorna el token y datos del usuario
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        
        // 1. Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya está en uso");
        }

        // 2. Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // 3. Buscar el rol USER (debe existir en la BD)
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado. Ejecuta el script de datos iniciales."));

        // 4. Crear el nuevo usuario
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        // 5. Guardar usuario en la BD
        userRepository.save(user);

        // 6. Generar token JWT
        String jwtToken = jwtService.generateToken(user);

        // 7. Retornar respuesta con token y permisos
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getRealUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .permissions(extractPermissions(user))
                .build();
    }

    /**
     * Autentica un usuario existente usando EMAIL y password
     * 
     * FLUJO:
     * 1. Busca el usuario por email
     * 2. Valida la contraseña
     * 3. Genera un token JWT
     * 4. Retorna el token y datos del usuario
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim();  // Sin toLowerCase - buscar exacto

        // 1. Buscar usuario por email
        Optional<User> userOptional;
        try {
            userOptional = userRepository.findByEmail(email);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar usuario: " + e.getMessage());
        }
        
        User user = userOptional
                .orElseThrow(() -> {
                    return new RuntimeException("Credenciales incorrectas");
                });
        
        // 2. Validar la contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        // 3. Verificar que el usuario esté habilitado
        if (!user.isEnabled()) {
            throw new RuntimeException("Usuario deshabilitado");
        }

        // 4. Generar token JWT
        String jwtToken = jwtService.generateToken(user);

        // 5. Retornar respuesta con token y permisos
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getRealUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .permissions(extractPermissions(user))
                .build();
    }
    
    /**
     * Extrae todos los permisos de un usuario a partir de sus roles
     * 
     * FLUJO:
     * 1. Recorre todos los roles del usuario
     * 2. Para cada rol, obtiene sus permisos
     * 3. Retorna un Set con los nombres de todos los permisos (sin duplicados)
     * 
     * EJEMPLO:
     * Usuario con rol ADMIN → ["READ_CLIENTS", "CREATE_CLIENTS", "DELETE_CLIENTS", ...]
     * Usuario con rol USER → ["READ_CLIENTS", "CREATE_CLIENTS"]
     * 
     * @param user Usuario del cual extraer los permisos
     * @return Set de nombres de permisos
     */
    private Set<String> extractPermissions(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
