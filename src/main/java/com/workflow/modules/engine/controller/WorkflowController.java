// WorkflowController.java
package com.workflow.modules.engine.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.analytics.dto.CuelloDeBotellaDTO;
import com.workflow.modules.analytics.dto.DeptAnalyticsDTO;
import com.workflow.modules.analytics.service.AnalyticsService;
import com.workflow.modules.engine.dto.ClaimarTareaRequest;
import com.workflow.modules.engine.dto.CompletarTareaRequest;
import com.workflow.modules.engine.dto.DerivarTramiteRequest;
import com.workflow.modules.engine.service.WorkflowEngine;
import com.workflow.modules.tasks.model.ActiveTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/engine")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Workflow Engine", description = "Motor de ejecución de procesos: derivar, reclamar y completar trámites")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowEngine engine;
    private final AnalyticsService analyticsService;

    // ── Motor de Workflow ────────────────────────────────────────────────────

    @Operation(summary = "Derivar trámite",
               description = "Avanza la instancia desde el nodo actual al siguiente según las condiciones del diagrama. " +
                             "Retorna null si se alcanzó el nodo END (proceso completado).")
    @PostMapping("/derivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'ANALISTA', 'EJECUTIVO')")
    public ResponseEntity<ApiResponse<ActiveTask>> derivar(@Valid @RequestBody DerivarTramiteRequest request) {
        ActiveTask tarea = engine.derivarTramite(request.getInstanciaId(), request.getFormData());
        if (tarea == null) {
            return ResponseEntity.ok(ApiResponse.ok("Proceso completado (nodo END alcanzado)", null));
        }
        return ResponseEntity.ok(ApiResponse.ok("Trámite derivado al siguiente nodo", tarea));
    }

    @Operation(summary = "Reclamar tarea",
               description = "El funcionario reclama una tarea pendiente: semáforo ROJO → AMARILLO.")
    @PostMapping("/claimar")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ANALISTA', 'EJECUTIVO', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<ActiveTask>> claimar(@Valid @RequestBody ClaimarTareaRequest request) {
        ActiveTask tarea = engine.claimarTarea(request.getTareaId(), request.getUsuarioId());
        return ResponseEntity.ok(ApiResponse.ok("Tarea reclamada (ROJO → AMARILLO)", tarea));
    }

    @Operation(summary = "Completar tarea",
               description = "El funcionario completa la tarea con datos del formulario: AMARILLO → VERDE. " +
                             "Archiva en historial y dispara automáticamente la siguiente transición.")
    @PostMapping("/completar")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ANALISTA', 'EJECUTIVO', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<ActiveTask>> completar(@Valid @RequestBody CompletarTareaRequest request) {
        ActiveTask tarea = engine.completarTarea(request.getTareaId(), request.getFormData());
        return ResponseEntity.ok(ApiResponse.ok("Tarea completada y archivada (AMARILLO → VERDE)", tarea));
    }

    // ── Analítica ────────────────────────────────────────────────────────────

    @Operation(summary = "Rendimiento por departamento",
               description = "Retorna duración promedio por nodo dentro de un departamento para detectar ineficiencias.")
    @GetMapping("/analytics/departamento/{idDepartamento}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<List<DeptAnalyticsDTO>>> analyticsPorDepartamento(
            @PathVariable String idDepartamento) {
        List<DeptAnalyticsDTO> resultado = analyticsService.getDuracionPromedioPorDepartamento(idDepartamento);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    @Operation(summary = "Cuellos de botella",
               description = "Retorna el top-5 de nodos con mayor tiempo promedio de resolución en todo el sistema.")
    @GetMapping("/analytics/cuellos-de-botella")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<List<CuelloDeBotellaDTO>>> cuellosDeBottella() {
        List<CuelloDeBotellaDTO> resultado = analyticsService.getCuellosDeBottella();
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
}
