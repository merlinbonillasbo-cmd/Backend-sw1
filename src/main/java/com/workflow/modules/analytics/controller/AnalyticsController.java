// AnalyticsController.java
package com.workflow.modules.analytics.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.analytics.dto.CuelloDeBotellaDTO;
import com.workflow.modules.analytics.dto.DeptAnalyticsDTO;
import com.workflow.modules.analytics.dto.RendimientoDeptDTO;
import com.workflow.modules.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Analytics", description = "Métricas y análisis de rendimiento de procesos")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Cuellos de botella",
               description = "Retorna el top-5 de nodos con mayor tiempo promedio de resolución en todo el sistema. " +
                             "Útil para identificar cuellos de botella a nivel organizacional.")
    @GetMapping("/cuellos")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<CuelloDeBotellaDTO>>> getCuellosDeBottella() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getCuellosDeBottella()));
    }

    @Operation(summary = "Rendimiento por departamento",
               description = "Retorna duración promedio por nodo dentro del departamento especificado. " +
                             "Incluye total de tareas, tareas retrasadas y porcentaje de retraso.")
    @GetMapping("/departamento/{idDepartamento}")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<DeptAnalyticsDTO>>> getRendimientoPorDepartamento(
            @PathVariable String idDepartamento) {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getDuracionPromedioPorDepartamento(idDepartamento)));
    }

    @Operation(summary = "Rendimiento por todos los departamentos",
               description = "Ranking de todos los departamentos con total de tareas, retrasadas y duración promedio.")
    @GetMapping("/rendimiento-departamentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<RendimientoDeptDTO>>> getRendimientoDepartamentos() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getRendimientoPorDepartamentos()));
    }
}
