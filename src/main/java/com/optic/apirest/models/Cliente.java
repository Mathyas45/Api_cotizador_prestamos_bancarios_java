package com.optic.apirest.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data//esto es para generar los metodos get y set
@Entity//esto es para indicar que es una entidad de base de datos
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre_completo" , length = 255, nullable = false)
    private String nombreCompleto;

    @Column(name="documento_identidad" , length = 20, nullable = false)
    private String documentoIdentidad;

    @Column(length = 255, nullable = true)
    private String email;

    @Column(length = 20, nullable = false)
    private String telefono;

    @Column(name = "ingreso_mensual", length = 500, nullable = true, precision = 10, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "reg_estado", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer regEstado;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Se establece SOLO al actualizar (UPDATE), no al crear
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @JsonManagedReference//para evitar referencia ciclica en la relacion uno a muchos esto hace referencia a las solicitudes de prestamo que tiene el cliente, el cliente puede tener muchas solicitudes de prestamo
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<SolicitudPrestamo> solicitudesPrestamo;


    // Este método se ejecuta automáticamente ANTES de cada UPDATE
    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



}
