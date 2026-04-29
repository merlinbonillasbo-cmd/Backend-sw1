// TareaController.java
package com.workflow.modules.tasks.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.tasks.model.ActiveTask;
import com.workflow.modules.tasks.service.TareaService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/tareas")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Tareas", description = "Gestión de tareas activas en el workflow")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @Operation(summary = "Listar tareas", description = "Retorna lista paginada de todas las tareas activas")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActiveTask>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(tareaService.findAll(pageable)));
    }

    @Operation(summary = "Obtener tarea por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActiveTask>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(tareaService.findById(id)));
    }

    @Operation(summary = "Listar tareas por departamento",
               description = "Retorna todas las tareas activas asignadas a un departamento específico")
    @GetMapping("/departamento/{idDepartamento}")
    public ResponseEntity<ApiResponse<List<ActiveTask>>> findByDepartamento(
            @PathVariable String idDepartamento) {
        return ResponseEntity.ok(ApiResponse.ok(tareaService.findByDepartamento(idDepartamento)));
    }

    @Operation(summary = "Listar tareas por instancia de proceso")
    @GetMapping("/instancia/{idInstancia}")
    public ResponseEntity<ApiResponse<List<ActiveTask>>> findByInstancia(@PathVariable String idInstancia) {
        return ResponseEntity.ok(ApiResponse.ok(tareaService.findByInstancia(idInstancia)));
    }

    @Operation(summary = "Listar tareas por usuario asignado")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<ApiResponse<List<ActiveTask>>> findByUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(ApiResponse.ok(tareaService.findByUsuario(idUsuario)));
    }

    @Operation(summary = "Crear nueva tarea")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<ActiveTask>> create(@Valid @RequestBody ActiveTask tarea) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tarea creada exitosamente", tareaService.create(tarea)));
    }

    @Operation(summary = "Actualizar tarea completa")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'ANALISTA', 'EJECUTIVO')")
    public ResponseEntity<ApiResponse<ActiveTask>> update(
            @PathVariable String id, @Valid @RequestBody ActiveTask tarea) {
        return ResponseEntity.ok(ApiResponse.ok("Tarea actualizada", tareaService.update(id, tarea)));
    }

    @Operation(summary = "Actualizar tarea parcialmente",
               description = "Permite actualizar campos específicos como semáforo, prioridad o datos del formulario")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'ANALISTA', 'EJECUTIVO')")
    public ResponseEntity<ApiResponse<ActiveTask>> patch(
            @PathVariable String id, @RequestBody ActiveTask partial) {
        return ResponseEntity.ok(ApiResponse.ok("Tarea actualizada parcialmente", tareaService.patch(id, partial)));
    }

    @Operation(summary = "Eliminar tarea")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        tareaService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Tarea eliminada", null));
    }
}
