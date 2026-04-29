// DepartamentoController.java
package com.workflow.modules.departments.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.departments.dto.DepartamentoRequest;
import com.workflow.modules.departments.model.Department;
import com.workflow.modules.departments.service.DepartmentService;
import com.workflow.modules.users.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/departamentos")
@Tag(name = "Departamentos", description = "Gestión de departamentos organizacionales")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartmentService departmentService;

    @Operation(summary = "Listar departamentos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.findAll()));
    }

    @Operation(summary = "Obtener departamento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.findById(id)));
    }

    @Operation(summary = "Listar usuarios activos de un departamento")
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> findUsuarios(@PathVariable String id) {
        List<Map<String, String>> result = departmentService.findUsuariosByDepartamento(id).stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "nombreCompleto", u.getNombreCompleto(),
                        "username", u.getNombreUsuario()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "Crear nuevo departamento")
    @PostMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Department>> create(@Valid @RequestBody DepartamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Departamento creado exitosamente", departmentService.create(request)));
    }

    @Operation(summary = "Actualizar departamento")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Department>> update(
            @PathVariable String id, @Valid @RequestBody DepartamentoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Departamento actualizado", departmentService.update(id, request)));
    }

    @Operation(summary = "Eliminar departamento")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Departamento eliminado", null));
    }
}

