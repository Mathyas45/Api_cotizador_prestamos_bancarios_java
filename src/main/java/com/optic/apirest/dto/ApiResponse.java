package com.optic.apirest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase genérica para estructurar las respuestas de la API.
 * 
 * @param <T> Tipo de dato que contendrá la respuesta
 * 
 * Ejemplo de uso:
 * ApiResponse<ClienteResponse> response = ApiResponse.<ClienteResponse>builder()
 *     .success(true)
 *     .message("Cliente encontrado")
 *     .data(clienteResponse)
 *     .build();
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
