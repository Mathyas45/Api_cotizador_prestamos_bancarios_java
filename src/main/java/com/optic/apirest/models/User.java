package com.optic.apirest.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad User - Representa un usuario del sistema
 * 
 * Implementa UserDetails de Spring Security para integrarse con el sistema de autenticación
 * 
 * RELACIONES:
 * - Many-to-Many con Role: Un usuario puede tener varios roles (ADMIN, USER, MANAGER)
 *   Ejemplo: Un usuario puede ser ADMIN y MANAGER a la vez
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // Para activar/desactivar usuarios

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relación Many-to-Many con Role
     * 
     * fetch = FetchType.EAGER: Carga los roles SIEMPRE que se carga el usuario
     * (necesario para Spring Security)
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Métodos de UserDetails ====================

    /**
     * Retorna todas las autoridades (permisos) del usuario, se guardan en roles y permisos.
     * Spring Security usa esto para autorización.
     * esto jala de los roles y permisos asignados al usuario y los convierte en GrantedAuthority que es en definición lo que spring security usa para manejar los permisos
     * @return colección de autoridades (roles y permisos) del usuario
     * GrantedAuthority es una interfaz que representa una autoridad otorgada al usuario, como un rol o permiso.
     * esto se llama cada vez que spring security necesita verificar si el usuario tiene permiso para hacer algo
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (roles != null) {
            for (Role role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                
                // Agregar todos los permisos de cada rol
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    }
                }
            }
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Spring Security usa este método para identificar al usuario.
     * Retornamos el EMAIL porque es lo que usamos para login.
     * 
     * IMPORTANTE: Aunque el método se llama getUsername(),
     * retornamos el email porque es nuestro identificador de login.
     */
    @Override
    public String getUsername() {
        return email;  // Usamos email para login
    }
    
    /**
     * Retorna el nombre de usuario real (username)
     * Usa este método cuando necesites el username, no getUsername()
     */
    public String getRealUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Puedes implementar lógica de expiración si lo necesitas
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Puedes implementar bloqueo de cuentas si lo necesitas
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puedes implementar expiración de contraseñas si lo necesitas
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
