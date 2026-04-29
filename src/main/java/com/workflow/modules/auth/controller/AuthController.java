package com.workflow.modules.auth.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.auth.dto.LoginRequest;
import com.workflow.modules.auth.dto.LoginResponse;
import com.workflow.modules.auth.dto.RegistroRequest;
import com.workflow.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Autenticación", description = "Endpoints de registro, login y logout")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registro del primer usuario (ADMIN)")
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<LoginResponse>> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Usuario administrador creado exitosamente", authService.registro(request)));
    }

    @Operation(summary = "Login con correo y contraseña")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", authService.login(request)));
    }

    @Operation(summary = "Cerrar sesión e invalidar token")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada exitosamente", null));
    }
}

