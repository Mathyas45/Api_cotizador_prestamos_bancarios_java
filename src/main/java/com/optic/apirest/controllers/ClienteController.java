package com.optic.apirest.controllers;

import com.optic.apirest.dto.ApiResponse;
import com.optic.apirest.dto.cliente.ClienteRequest;
import com.optic.apirest.dto.cliente.ClienteResponse;
import com.optic.apirest.services.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Clientes con protección de permisos
 * 
 * Permisos requeridos:
 * - READ_CLIENTS: Para listar y ver clientes
 * - CREATE_CLIENTS: Para crear nuevos clientes
 * - UPDATE_CLIENTS: Para actualizar clientes
 * - DELETE_CLIENTS: Para eliminar clientes
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;


    // Inyección por constructor (mejor práctica)
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ClienteResponse>> create(@RequestBody ClienteRequest request) {
        try {
            ClienteResponse response =  clienteService.create(request);
            return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                    .success(true)
                    .message("Cliente creado exitosamente")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<ClienteResponse>builder()
                    .success(false)
                    .message("Error al crear el cliente: " + e.getMessage())
                    .build());
        }
    }

    @PreAuthorize("hasAuthority('READ_CLIENTS')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> findById(@PathVariable Long id){
        try {
            ClienteResponse response = clienteService.findById(id);
            return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                    .success(true)
                    .message("Cliente encontrado")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.<ClienteResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PreAuthorize("hasAuthority('READ_CLIENTS')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> findAll(@RequestParam(required = false) String query) {
        try {
            List<ClienteResponse> response = clienteService.findAll(query);
            return ResponseEntity.ok(ApiResponse.<List<ClienteResponse>>builder()
                    .success(true)
                    .message("Clientes encontrados")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<List<ClienteResponse>>builder()
                    .success(false)
                    .message("Error al listar clientes: " + e.getMessage())
                    .build());
        }
    }

        @PreAuthorize("hasAuthority('UPDATE_CLIENTS')")
        @PutMapping("/update/{id}")
        public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id, @RequestBody ClienteRequest request) {
            try {
                clienteService.update(id, request);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                        .success(true)
                        .message("Cliente actualizado exitosamente")
                        .build());
            } catch (Exception e) {
                return ResponseEntity.status(500).body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Error al actualizar el cliente: " + e.getMessage())
                        .build());
            }
        }

    @PreAuthorize("hasAuthority('DELETE_CLIENTS')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            clienteService.delete(id);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Cliente eliminado exitosamente")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error al eliminar el cliente: " + e.getMessage())
                    .build());
        }
    }

}
