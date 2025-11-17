package com.optic.apirest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase para estructurar las respuestas de la API.
 */
@Data //esto genera getters y setters automáticamente
@AllArgsConstructor // Genera un constructor con todos los campos
@NoArgsConstructor  // Genera un constructor vacío que nos sirve para crear instancias sin parámetros, por ejemplo al deserializar JSON o XML, o cuando queremos crear un objeto y luego establecer sus propiedades.

public class ApiResponse {
    private String message;
    private int code;


}
