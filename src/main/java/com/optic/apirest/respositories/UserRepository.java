package com.optic.apirest.respositories;

import com.optic.apirest.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad User
 * 
 * JpaRepository proporciona métodos CRUD automáticos:
 * - save()
 * - findById()
 * - findAll()
 * - deleteById()
 * etc.
 * 
 * Los métodos personalizados se declaran aquí y Spring Data JPA
 * genera la implementación automáticamente
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por username
     * 
     * @param username nombre de usuario
     * @return Optional con el usuario si existe, vacío si no
     * 
     * Spring Security lo usa para autenticación
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por email
     * 
     * @param email correo electrónico
     * @return Optional con el usuario si existe, opcional es vacío si no
     */
    @Query(" SELECT u FROM User u WHERE u.email = :email ")
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con ese username
     * 
     * @param username nombre de usuario
     * @return true si existe, false si no
     * 
     * Útil para validar registros (evitar duplicados)
     */
    Boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con ese email
     * 
     * @param email correo electrónico
     * @return true si existe, false si no
     */
    Boolean existsByEmail(String email);
}
