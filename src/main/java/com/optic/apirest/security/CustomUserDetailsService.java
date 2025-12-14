package com.optic.apirest.security;

import com.optic.apirest.respositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio UserDetails - Carga información del usuario para Spring Security
 * 
 * Spring Security usa esta interfaz para obtener los datos del usuario
 * durante el proceso de autenticación
 * 
 * FLUJO:
 * 1. Usuario envía EMAIL + password
 * 2. Spring Security llama a loadUserByUsername(email) - el nombre del método es confuso pero usamos EMAIL
 * 3. Retornamos el usuario de la BD
 * 4. Spring Security compara la password del request con la de la BD
 * 5. Si coincide, autenticación exitosa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su EMAIL
     * 
     * NOTA: El método se llama loadUserByUsername por la interfaz de Spring,
     * pero nosotros usamos el EMAIL como identificador de login.
     * 
     * Este método es llamado automáticamente por Spring Security
     * durante el proceso de autenticación
     * 
     * @param email correo electrónico del usuario
     * @return UserDetails con información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new UsernameNotFoundException("Usuario no encontrado con email: " + email);
                });
    }
}
