// PoliticaController.java
package com.workflow.modules.workflowdefinition.controller;

import com.workflow.common.ApiResponse;
import com.workflow.common.Exceptions;
import com.workflow.modules.workflowdefinition.dto.WorkflowCanvasDto;
import com.workflow.modules.workflowdefinition.model.Node;
import com.workflow.modules.workflowdefinition.model.WorkflowDefinition;
import com.workflow.modules.workflowdefinition.service.WorkflowDefinitionService;
import com.workflow.modules.users.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.workflow.modules.users.model.User;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/politicas")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Políticas de Workflow", description = "Gestión de definiciones de workflow (políticas de proceso)")
@RequiredArgsConstructor
public class PoliticaController {

    private final WorkflowDefinitionService service;
    private final SimpMessagingTemplate messaging;
    private final UserRepository userRepository;

    @Operation(summary = "Listar políticas", description = "Retorna lista paginada de todas las definiciones de workflow")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.findAll()));
    }

    @Operation(summary = "Obtener política por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> findById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        WorkflowDefinition def = service.findById(id);
        validarAcceso(def, obtenerUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok(def));
    }

    @Operation(summary = "Obtener nodos del diagrama de una política",
               description = "Retorna la lista de nodos que componen el diagrama de la política especificada")
    @GetMapping("/{id}/nodos")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<List<Node>>> getNodos(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        WorkflowDefinition def = service.findById(id);
        validarAcceso(def, obtenerUserId(principal));
        List<Node> nodos = def.getNodos();
        return ResponseEntity.ok(ApiResponse.ok(nodos));
    }

    @Operation(summary = "Crear nueva política de workflow")
    @PostMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> create(
            @Valid @RequestBody WorkflowCanvasDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String userId = obtenerUserId(principal);
        if (userId == null || userId.isBlank()) {
            throw new Exceptions.UnauthorizedException("Usuario no autenticado.");
        }
        dto.setIdPropietario(userId);
        if (dto.getColaboradores() == null) {
            dto.setColaboradores(List.of());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Política creada exitosamente", service.create(dto)));
    }

    @Operation(summary = "Actualizar política completa")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> update(
            @PathVariable String id,
            @Valid @RequestBody WorkflowCanvasDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String userId = obtenerUserId(principal);
        WorkflowDefinition existente = service.findById(id);
        validarAcceso(existente, userId);
        if (!esPropietario(existente, userId)
            && dto.getColaboradores() != null
            && !colaboradoresIguales(existente.getColaboradores(), dto.getColaboradores())) {
            throw new Exceptions.ForbiddenException("Solo el propietario puede modificar colaboradores.");
        }

        WorkflowDefinition actualizado = service.update(id, dto);

        // Emitir por WebSocket para sincronizar otros diseñadores conectados
        String editadoPor = principal != null ? principal.getUsername() : "desconocido";
        String editadoPorNombre = principal instanceof User u ? u.getNombreCompleto() : editadoPor;
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("tipo",             "DIAGRAMA_ACTUALIZADO");
        payload.put("flujoId",          id);
        payload.put("xmlBpmn",          actualizado.getXmlBpmn() != null ? actualizado.getXmlBpmn() : "");
        payload.put("editadoPor",       editadoPor);
        payload.put("editadoPorNombre", editadoPorNombre);
        payload.put("timestamp",        Instant.now().toString());
        messaging.convertAndSend("/topic/pizarra/" + id, (Object) payload);

        return ResponseEntity.ok(ApiResponse.ok("Política actualizada", actualizado));
    }

    @Operation(summary = "Actualizar política parcialmente")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> patch(
            @PathVariable String id,
            @RequestBody WorkflowCanvasDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        String userId = obtenerUserId(principal);
        WorkflowDefinition existente = service.findById(id);
        validarAcceso(existente, userId);
        if (!esPropietario(existente, userId)
            && dto.getColaboradores() != null
            && !colaboradoresIguales(existente.getColaboradores(), dto.getColaboradores())) {
            throw new Exceptions.ForbiddenException("Solo el propietario puede modificar colaboradores.");
        }

        WorkflowDefinition actualizado = service.update(id, dto);

        String editadoPor = principal != null ? principal.getUsername() : "desconocido";
        String editadoPorNombre = principal instanceof User u ? u.getNombreCompleto() : editadoPor;
        Map<String, Object> payload2 = new java.util.HashMap<>();
        payload2.put("tipo",             "DIAGRAMA_ACTUALIZADO");
        payload2.put("flujoId",          id);
        payload2.put("xmlBpmn",          actualizado.getXmlBpmn() != null ? actualizado.getXmlBpmn() : "");
        payload2.put("editadoPor",       editadoPor);
        payload2.put("editadoPorNombre", editadoPorNombre);
        payload2.put("timestamp",        Instant.now().toString());
        messaging.convertAndSend("/topic/pizarra/" + id, (Object) payload2);

        return ResponseEntity.ok(ApiResponse.ok("Política actualizada parcialmente", actualizado));
    }

    @Operation(summary = "Publicar política (cambiar estado a ACTIVO)")
    @PostMapping("/{id}/publicar")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> publish(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        WorkflowDefinition def = service.findById(id);
        validarAcceso(def, obtenerUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Política publicada exitosamente", service.publish(id)));
    }

    @Operation(summary = "Revertir política a borrador (cambiar estado a BORRADOR)")
    @PostMapping("/{id}/revertir")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> revertir(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        WorkflowDefinition def = service.findById(id);
        validarAcceso(def, obtenerUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Política revertida a borrador", service.revertir(id)));
    }

    @Operation(summary = "Eliminar política")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        WorkflowDefinition def = service.findById(id);
        validarAcceso(def, obtenerUserId(principal));
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Política eliminada", null));
    }

    private void validarAcceso(WorkflowDefinition def, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new Exceptions.UnauthorizedException("Usuario no autenticado.");
        }
        if (def.getIdPropietario() == null || def.getIdPropietario().isBlank()) {
            return; // flujo legado sin propietario asignado
        }
        if (esPropietario(def, userId)) return;
        List<String> colaboradores = def.getColaboradores();
        if (colaboradores != null && colaboradores.contains(userId)) return;
        throw new Exceptions.ForbiddenException("No tienes acceso a este flujo.");
    }

    private boolean esPropietario(WorkflowDefinition def, String userId) {
        return def.getIdPropietario() != null && def.getIdPropietario().equals(userId);
    }

    private boolean colaboradoresIguales(List<String> actual, List<String> nuevo) {
        List<String> a = actual == null ? List.of() : actual;
        List<String> b = nuevo == null ? List.of() : nuevo;
        return a.size() == b.size() && a.containsAll(b) && b.containsAll(a);
    }

    private String obtenerUserId(UserDetails principal) {
        if (principal instanceof User u) {
            return u.getId();
        }
        String correo = principal != null ? principal.getUsername() : null;
        if (correo == null) {
            return null;
        }
        return userRepository.findByCorreoIgnoreCase(correo).map(User::getId).orElse(null);
    }
}
