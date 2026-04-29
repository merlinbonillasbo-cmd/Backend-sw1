// SnapshotController.java
package com.workflow.modules.snapshots.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.snapshots.model.DiagramSnapshot;
import com.workflow.modules.snapshots.service.SnapshotService;
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
@RequestMapping("/api/v1/snapshots")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Snapshots", description = "Gestión de snapshots de diagramas de workflow")
@RequiredArgsConstructor
public class SnapshotController {

    private final SnapshotService snapshotService;

    @Operation(summary = "Listar snapshots", description = "Retorna lista paginada de todos los snapshots")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DiagramSnapshot>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(snapshotService.findAll(pageable)));
    }

    @Operation(summary = "Obtener snapshot por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiagramSnapshot>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(snapshotService.findById(id)));
    }

    @Operation(summary = "Listar snapshots de una política", description = "Retorna snapshots ordenados por versión descendente")
    @GetMapping("/politica/{idPolitica}")
    public ResponseEntity<ApiResponse<List<DiagramSnapshot>>> findByPolitica(@PathVariable String idPolitica) {
        return ResponseEntity.ok(ApiResponse.ok(snapshotService.findByPolitica(idPolitica)));
    }

    @Operation(summary = "Crear nuevo snapshot")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'ANALISTA')")
    public ResponseEntity<ApiResponse<DiagramSnapshot>> create(@Valid @RequestBody DiagramSnapshot snapshot) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Snapshot creado exitosamente", snapshotService.create(snapshot)));
    }

    @Operation(summary = "Actualizar snapshot completo")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<DiagramSnapshot>> update(
            @PathVariable String id, @Valid @RequestBody DiagramSnapshot snapshot) {
        return ResponseEntity.ok(ApiResponse.ok("Snapshot actualizado", snapshotService.update(id, snapshot)));
    }

    @Operation(summary = "Actualizar snapshot parcialmente")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<DiagramSnapshot>> patch(
            @PathVariable String id, @RequestBody DiagramSnapshot partial) {
        return ResponseEntity.ok(ApiResponse.ok("Snapshot actualizado parcialmente", snapshotService.patch(id, partial)));
    }

    @Operation(summary = "Eliminar snapshot")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        snapshotService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Snapshot eliminado", null));
    }
}
