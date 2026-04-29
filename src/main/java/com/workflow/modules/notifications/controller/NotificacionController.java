// NotificacionController.java
package com.workflow.modules.notifications.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.notifications.model.Notification;
import com.workflow.modules.notifications.service.NotificacionService;
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
@RequestMapping("/api/v1/notificaciones")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Notificaciones", description = "Gestión de notificaciones de usuario")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Listar notificaciones", description = "Retorna lista paginada de todas las notificaciones")
    @GetMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Page<Notification>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(notificacionService.findAll(pageable)));
    }

    @Operation(summary = "Obtener notificación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Notification>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(notificacionService.findById(id)));
    }

    @Operation(summary = "Listar notificaciones de un usuario")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<ApiResponse<List<Notification>>> findByUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(ApiResponse.ok(notificacionService.findByUsuario(idUsuario)));
    }

    @Operation(summary = "Listar notificaciones no leídas de un usuario")
    @GetMapping("/usuario/{idUsuario}/no-leidas")
    public ResponseEntity<ApiResponse<List<Notification>>> findNoLeidas(@PathVariable String idUsuario) {
        return ResponseEntity.ok(ApiResponse.ok(notificacionService.findNoLeidasByUsuario(idUsuario)));
    }

    @Operation(summary = "Contar notificaciones no leídas de un usuario")
    @GetMapping("/usuario/{idUsuario}/no-leidas/count")
    public ResponseEntity<ApiResponse<Long>> countNoLeidas(@PathVariable String idUsuario) {
        return ResponseEntity.ok(ApiResponse.ok(notificacionService.countNoLeidas(idUsuario)));
    }

    @Operation(summary = "Crear nueva notificación")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'JEFE_DEPARTAMENTO')")
    public ResponseEntity<ApiResponse<Notification>> create(@Valid @RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Notificación creada", notificacionService.create(notification)));
    }

    @Operation(summary = "Actualizar notificación completa")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Notification>> update(
            @PathVariable String id, @Valid @RequestBody Notification notification) {
        return ResponseEntity.ok(ApiResponse.ok("Notificación actualizada", notificacionService.update(id, notification)));
    }

    @Operation(summary = "Actualizar notificación parcialmente")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Notification>> patch(
            @PathVariable String id, @RequestBody Notification partial) {
        return ResponseEntity.ok(ApiResponse.ok("Notificación actualizada parcialmente", notificacionService.patch(id, partial)));
    }

    @Operation(summary = "Marcar notificación como leída")
    @PatchMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<Notification>> marcarComoLeida(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Notificación marcada como leída", notificacionService.marcarComoLeida(id)));
    }

    @Operation(summary = "Eliminar notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        notificacionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Notificación eliminada", null));
    }
}
