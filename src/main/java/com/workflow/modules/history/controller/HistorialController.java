// HistorialController.java
package com.workflow.modules.history.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.history.model.AuditLog;
import com.workflow.modules.history.service.HistorialService;
import com.workflow.modules.tasks.model.TaskHistory;
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
@RequestMapping("/api/v1/historial")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Historial", description = "Gestión del historial de auditoría y registro de actividades")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    @Operation(summary = "Listar registros de historial", description = "Retorna lista paginada de todos los registros de auditoría")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(historialService.findAllAuditLogs(pageable)));
    }

    @Operation(summary = "Obtener registro de historial por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'ANALISTA')")
    public ResponseEntity<ApiResponse<AuditLog>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(historialService.findAuditLogById(id)));
    }

    @Operation(summary = "Listar historial por instancia de proceso")
    @GetMapping("/instancia/{idInstancia}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findByInstancia(@PathVariable String idInstancia) {
        return ResponseEntity.ok(ApiResponse.ok(historialService.findByInstancia(idInstancia)));
    }

    @Operation(summary = "Listar historial por actor")
    @GetMapping("/actor/{idActor}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> findByActor(@PathVariable String idActor) {
        return ResponseEntity.ok(ApiResponse.ok(historialService.findByActor(idActor)));
    }

    @Operation(summary = "Listar historial de tareas por instancia")
    @GetMapping("/tareas/instancia/{idInstancia}")
    public ResponseEntity<ApiResponse<List<TaskHistory>>> findTaskHistoryByInstancia(
            @PathVariable String idInstancia) {
        return ResponseEntity.ok(ApiResponse.ok(historialService.findTaskHistoryByInstancia(idInstancia)));
    }

    @Operation(summary = "Listar historial de tareas por política")
    @GetMapping("/tareas/politica/{idPolitica}")
    public ResponseEntity<ApiResponse<List<TaskHistory>>> findTaskHistoryByPolitica(
            @PathVariable String idPolitica) {
        return ResponseEntity.ok(ApiResponse.ok(historialService.findTaskHistoryByPolitica(idPolitica)));
    }

    @Operation(summary = "Listar historial de tareas por departamento")
    @GetMapping("/tareas/departamento/{idDepartamento}")
    public ResponseEntity<ApiResponse<List<TaskHistory>>> findTaskHistoryByDepartamento(
            @PathVariable String idDepartamento) {
        return ResponseEntity.ok(ApiResponse.ok(historialService.findTaskHistoryByDepartamento(idDepartamento)));
    }

    @Operation(summary = "Crear registro de auditoría")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'EJECUTIVO')")
    public ResponseEntity<ApiResponse<AuditLog>> create(@Valid @RequestBody AuditLog auditLog) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Registro creado", historialService.create(auditLog)));
    }

    @Operation(summary = "Actualizar registro de auditoría completo")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<AuditLog>> update(
            @PathVariable String id, @Valid @RequestBody AuditLog auditLog) {
        return ResponseEntity.ok(ApiResponse.ok("Registro actualizado", historialService.update(id, auditLog)));
    }

    @Operation(summary = "Actualizar registro de auditoría parcialmente")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<AuditLog>> patch(
            @PathVariable String id, @RequestBody AuditLog partial) {
        return ResponseEntity.ok(ApiResponse.ok("Registro actualizado parcialmente", historialService.patch(id, partial)));
    }

    @Operation(summary = "Eliminar registro de auditoría")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        historialService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Registro eliminado", null));
    }
}
