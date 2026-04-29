// UsuarioController.java
package com.workflow.modules.users.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.users.dto.UserRequest;
import com.workflow.modules.users.dto.UserResponse;
import com.workflow.modules.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RequiredArgsConstructor
public class UsuarioController {

    private final UserService userService;

    @Operation(summary = "Listar usuarios", description = "Retorna lista paginada de todos los usuarios")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAll()));
    }

    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }

    @Operation(summary = "Crear nuevo usuario")
    @PostMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Usuario creado exitosamente", userService.createByAdmin(request)));
    }

    @Operation(summary = "Actualizar usuario completo")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable String id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", userService.update(id, request)));
    }

    @Operation(summary = "Actualizar usuario parcialmente")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<UserResponse>> patch(
            @PathVariable String id, @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado parcialmente", userService.update(id, request)));
    }

    @Operation(summary = "Eliminar usuario")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado", null));
    }

    @Operation(summary = "Reactivar usuario")
    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> reactivate(@PathVariable String id) {
        userService.reactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario reactivado", null));
    }
}
