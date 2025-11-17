package com.optic.apirest.controllers;

import com.optic.apirest.dto.ApiResponse;
import com.optic.apirest.dto.cliente.ClienteRequest;
import com.optic.apirest.dto.cliente.ClienteResponse;
import com.optic.apirest.services.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;


    // Inyección por constructor (mejor práctica)
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> create(@RequestBody ClienteRequest request) {
        try {
            clienteService.create(request);
            return ResponseEntity.ok(new ApiResponse("Cliente creado exitosamente", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error al crear el cliente: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> findById(@PathVariable Long id){
        ClienteResponse response = clienteService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> findAll(@RequestParam(required = false) String query) {
        List<ClienteResponse> response = clienteService.findAll(query);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody ClienteRequest request) {
        try {
          clienteService.update(id, request);
            return ResponseEntity.ok(new ApiResponse("Cliente actualizado exitosamente", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error al actualizar el cliente: " + e.getMessage(), 500));
        }

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        try {
            clienteService.delete(id);
            // Lógica para eliminar un cliente
            return ResponseEntity.ok(new ApiResponse("Cliente eliminado exitosamente", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("Error al eliminar el cliente: " + e.getMessage(), 500));
        }

    }

}
