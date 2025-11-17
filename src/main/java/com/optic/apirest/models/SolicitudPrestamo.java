package com.optic.apirest.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data//esto es para generar los metodos get y set
@Entity//esto es para indicar que es una entidad de base de datos
@Table(name = "solicitudes_prestamo")
public class SolicitudPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = true, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "porcentaje_cuota_inicial", length = 500, nullable = true, precision = 5, scale = 2)
    private BigDecimal porcentajeCuotaInicial;

    @Column(name = "monto_cuota_inicial", length = 500, nullable = true, precision = 10, scale = 2)
    private BigDecimal montoCuotaInicial;

    @Column(name = "monto_financiar", length = 500, nullable = true, precision = 10, scale = 2)
    private BigDecimal montoFinanciar;

    @Column(name = "plazo_anios", nullable = true)
    private Integer plazoAnios;

    @Column(name = "tasa_interes", length = 500, nullable = true, precision = 5, scale = 2)
    private BigDecimal tasaInteres;

    @Column(length = 500, nullable = true, precision = 5, scale = 2)
    private BigDecimal tcea;

    @Column(name = "cuota_mensual", length = 500, nullable = true, precision = 10, scale = 2)
    private BigDecimal cuotaMensual;

    @Column(name = "motivo_rechazo", length = 1000, nullable = true)
    private String motivoRechazo;

    @Comment("Estado de la solicitud: 0 - Pendiente, 1 - Aprobado, 2 - Rechazado")
    @Column(nullable = true)
    private Integer estado; // 0: Pendiente, 1: Aprobado, 2: Rechazado

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Se establece SOLO al actualizar (UPDATE), no al crear
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonBackReference//esto es para referencia ciclica en la relacion muchos a uno nos sirve paradecir el cliente al que pertenece la solicitud y solo puede haber una solicitud por cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Este método se ejecuta automáticamente ANTES de cada UPDATE
    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



}
