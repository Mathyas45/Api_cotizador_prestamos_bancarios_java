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
 * Entidad Permission - Define acciones específicas que se pueden realizar
 * 
 * Ejemplos de permisos:
 * - READ_CLIENTS: Puede leer/listar clientes
 * - CREATE_CLIENTS: Puede crear nuevos clientes
 * - UPDATE_CLIENTS: Puede actualizar clientes existentes
 * - DELETE_CLIENTS: Puede eliminar clientes
 * - APPROVE_LOANS: Puede aprobar solicitudes de préstamo
 * - REJECT_LOANS: Puede rechazar solicitudes de préstamo
 * 
 * RELACIONES:
 * - Many-to-Many con Role: Los permisos se agrupan en roles
 * 
 * GRANULARIDAD: Esto te da control fino sobre QUÉ puede hacer cada usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name; // READ_CLIENTS, CREATE_LOAN, etc.

    @Column(length = 255)
    private String description; // Descripción legible del permiso

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Relación inversa con Role
     * 
     * mappedBy: La relación es manejada por Role
     * LAZY: No se carga automáticamente para evitar bucles
     * JsonIgnore: Evita serialización circular
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
