package com.optic.apirest.respositories;

import com.optic.apirest.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//jpa es una interfaz que nos permite hacer operaciones CRUD en la base de datos se usa en spring como una capa de abstraccion, tencnicamente es un ORM (Object Relational Mapping) que nos permite mapear objetos java a tablas de base de datos relacionales
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsBydocumentoIdentidad(String documentoIdentidad);//Verifica si existe un dni con el nombre proporcionado


    @Query("SELECT c.documentoIdentidad FROM Cliente c WHERE c.id = :id")
    String findDocumentoIdentidadById(Long id);

    Cliente findClienteByDocumentoIdentidad(String documentoIdentidad);

    List<Cliente> findByNombreCompletoContainingIgnoreCaseOrDocumentoIdentidadContainingIgnoreCase(String nombreCompleto, String documentoIdentidad); //buscar por nombre o apellido ignorando mayusculas y minusculas

}
