package com.optic.apirest.respositories;

import com.optic.apirest.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Permission
 * 
 * Permite gestionar permisos del sistema
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Busca un permiso por nombre
     * 
     * @param name nombre del permiso (READ_CLIENTS, CREATE_LOAN, etc.)
     * @return Optional con el permiso si existe
     */
    Optional<Permission> findByName(String name);

    /**
     * Verifica si existe un permiso con ese nombre
     * 
     * @param name nombre del permiso
     * @return true si existe, false si no
     */
    Boolean existsByName(String name);
}
