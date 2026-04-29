package com.workflow.modules.quejas.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.quejas.model.Queja;
import com.workflow.modules.quejas.repository.QuejaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quejas")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Quejas", description = "Gestión de quejas y observaciones de tareas")
@RequiredArgsConstructor
public class QuejasController {

    private final QuejaRepository repository;

    @Operation(summary = "Crear nueva queja")
    @PostMapping
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<ApiResponse<Queja>> crear(@RequestBody Queja queja) {
        Queja saved = repository.save(queja);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Queja registrada exitosamente", saved));
    }

    @Operation(summary = "Listar quejas del usuario logueado")
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<ApiResponse<List<Queja>>> listarPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(ApiResponse.ok(repository.findByIdUsuarioOrderByFechaCreacionDesc(idUsuario)));
    }

    @Operation(summary = "Listar quejas de un departamento")
    @GetMapping("/departamento/{codigo}")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<List<Queja>>> listarPorDepartamento(@PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.ok(repository.findByDepartamentoCodigoOrderByFechaCreacionDesc(codigo)));
    }
}
