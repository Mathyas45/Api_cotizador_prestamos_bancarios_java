package com.optic.apirest.respositories;

import com.optic.apirest.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Role
 * 
 * Permite buscar roles por nombre para asignarlos a usuarios
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Busca un rol por nombre
     * 
     * @param name nombre del rol (ADMIN, USER, MANAGER)
     * @return Optional con el rol si existe
     * 
     * Usado al registrar usuarios para asignarles el rol USER por defecto
     */
    Optional<Role> findByName(String name);

    /**
     * Verifica si existe un rol con ese nombre
     * 
     * @param name nombre del rol
     * @return true si existe, false si no
     */
    Boolean existsByName(String name);
}
