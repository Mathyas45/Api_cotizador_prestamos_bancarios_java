package com.optic.apirest.respositories;

import com.optic.apirest.models.SolicitudPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudPrestamoRepository extends JpaRepository<SolicitudPrestamo, Long> {

    @Query("SELECT sp FROM SolicitudPrestamo sp JOIN sp.cliente c " +
            "WHERE (:clienteId IS NULL OR c.id = :clienteId) " +
            "AND (:nombre IS NULL OR LOWER(c.nombreCompleto) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:documento IS NULL OR c.documentoIdentidad = :documento)")
    List<SolicitudPrestamo> buscarPorClienteIdNombreODocumento(
            @Param("nombre") String nombre,
            @Param("documento") String documento
    );

     //countByEstado
    Long  countByEstado(Integer estado);

        @Query(value = """
            SELECT DATE_FORMAT(s.created_at, '%Y-%m') AS mes,
                COUNT(s.id) AS total
            FROM solicitudes_prestamo s
            WHERE s.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
            GROUP BY DATE_FORMAT(s.created_at, '%Y-%m')
            ORDER BY DATE_FORMAT(s.created_at, '%Y-%m')
        """, nativeQuery = true)
        List<Object[]> solicitudesPorMes();

        @Query(value = """
        SELECT DATE_FORMAT(s.created_at, '%Y-%m') AS mes,
               SUM(CASE WHEN s.estado = 1 THEN 1 ELSE 0 END) AS aprobados,
               SUM(CASE WHEN s.estado = 0 THEN 1 ELSE 0 END) AS rechazados
        FROM solicitudes_prestamo s
        WHERE s.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
        GROUP BY mes
        ORDER BY mes
    """, nativeQuery = true)
        List<Object[]> aprobadosRechazadosPorMes();

}
