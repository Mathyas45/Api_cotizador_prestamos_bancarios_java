package com.optic.apirest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Role - Define tipos de usuario en el sistema
 * 
 * Ejemplos de roles: ADMIN, USER, MANAGER, AUDITOR
 * 
 * RELACIONES:
 * - Many-to-Many con User: Muchos usuarios pueden tener el mismo rol
 * - Many-to-Many con Permission: Un rol agrupa varios permisos
 * 
 * VENTAJA: Si cambias los permisos de un rol, TODOS los usuarios
 * con ese rol automáticamente obtienen los cambios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // ADMIN, USER, MANAGER

    @Column(length = 255)
    private String description; // Descripción del rol

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Relación Many-to-Many con Permission
     * 
     * @ManyToMany: Un rol puede tener muchos permisos Y un permiso puede estar en muchos roles
     * 
     * TABLA INTERMEDIA (role_permissions):
     * - role_id: FK a roles.id
     * - permission_id: FK a permissions.id
     * 
     * fetch = FetchType.EAGER: Carga los permisos siempre que se carga el rol
     * 
     * EJEMPLO:
     * Role ADMIN podría tener: READ_CLIENTS, CREATE_CLIENTS, DELETE_CLIENTS
     * Role USER podría tener: READ_CLIENTS
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Relación inversa con User
     * 
     * mappedBy: Indica que la relación es manejada por el lado de User
     * LAZY: No se carga automáticamente para evitar bucles
     * JsonIgnore: Evita serialización circular
     * ToString.Exclude y EqualsAndHashCode.Exclude: Evita bucles infinitos en Lombok
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<User> users = new HashSet<>();
}
