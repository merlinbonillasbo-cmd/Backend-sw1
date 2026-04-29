// SesionController.java
package com.workflow.modules.sessions.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.sessions.model.Session;
import com.workflow.modules.sessions.service.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/sesiones")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Sesiones", description = "Gestión de sesiones de usuario")
@RequiredArgsConstructor
public class SesionController {

    private final SesionService sesionService;

    @Operation(summary = "Listar sesiones", description = "Retorna lista paginada de todas las sesiones activas")
    @GetMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Page<Session>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(sesionService.findAll(pageable)));
    }

    @Operation(summary = "Obtener sesión por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Session>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(sesionService.findById(id)));
    }

    @Operation(summary = "Crear nueva sesión")
    @PostMapping
    public ResponseEntity<ApiResponse<Session>> create(@Valid @RequestBody Session session) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Sesión creada", sesionService.create(session)));
    }

    @Operation(summary = "Actualizar sesión completa")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Session>> update(
            @PathVariable String id, @Valid @RequestBody Session session) {
        return ResponseEntity.ok(ApiResponse.ok("Sesión actualizada", sesionService.update(id, session)));
    }

    @Operation(summary = "Actualizar sesión parcialmente")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Session>> patch(
            @PathVariable String id, @RequestBody Session partial) {
        return ResponseEntity.ok(ApiResponse.ok("Sesión actualizada parcialmente", sesionService.patch(id, partial)));
    }

    @Operation(summary = "Eliminar sesión")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sesionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Sesión eliminada", null));
    }

    @Operation(summary = "Cerrar todas las sesiones de un usuario")
    @DeleteMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> deleteByUsuario(@PathVariable String idUsuario) {
        sesionService.deleteByUsuario(idUsuario);
        return ResponseEntity.ok(ApiResponse.ok("Sesiones del usuario eliminadas", null));
    }
}
