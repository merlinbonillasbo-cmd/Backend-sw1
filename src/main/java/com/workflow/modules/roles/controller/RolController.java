// RolController.java
package com.workflow.modules.roles.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.roles.model.RolePermission;
import com.workflow.modules.roles.service.RolService;
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
@RequestMapping("/api/v1/roles")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Roles y Permisos", description = "Gestión de roles y permisos del sistema")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @Operation(summary = "Listar roles", description = "Retorna lista paginada de todos los roles")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RolePermission>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(rolService.findAll(pageable)));
    }

    @Operation(summary = "Obtener rol por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RolePermission>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(rolService.findById(id)));
    }

    @Operation(summary = "Obtener rol por nombre")
    @GetMapping("/nombre/{nombreRol}")
    public ResponseEntity<ApiResponse<RolePermission>> findByNombre(@PathVariable String nombreRol) {
        return ResponseEntity.ok(ApiResponse.ok(rolService.findByNombreRol(nombreRol)));
    }

    @Operation(summary = "Crear nuevo rol")
    @PostMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<RolePermission>> create(@Valid @RequestBody RolePermission rol) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Rol creado exitosamente", rolService.create(rol)));
    }

    @Operation(summary = "Actualizar rol completo")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<RolePermission>> update(
            @PathVariable String id, @Valid @RequestBody RolePermission rol) {
        return ResponseEntity.ok(ApiResponse.ok("Rol actualizado", rolService.update(id, rol)));
    }

    @Operation(summary = "Actualizar rol parcialmente")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<RolePermission>> patch(
            @PathVariable String id, @RequestBody RolePermission partial) {
        return ResponseEntity.ok(ApiResponse.ok("Rol actualizado parcialmente", rolService.patch(id, partial)));
    }

    @Operation(summary = "Eliminar rol")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        rolService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Rol eliminado", null));
    }
}
