package com.workflow.modules.auth.service;

import com.workflow.common.Exceptions;
import com.workflow.model.enums.UserRole;
import com.workflow.modules.auth.dto.LoginRequest;
import com.workflow.modules.auth.dto.LoginResponse;
import com.workflow.modules.auth.dto.RegistroRequest;
import com.workflow.modules.departments.model.Department;
import com.workflow.modules.departments.service.DepartmentService;
import com.workflow.modules.sessions.model.Session;
import com.workflow.modules.sessions.repository.SessionRepository;
import com.workflow.modules.users.model.User;
import com.workflow.modules.users.repository.UserRepository;
import com.workflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;

    public AuthService(UserRepository userRepository,
                       SessionRepository sessionRepository,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder,
                       DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
    }

    public LoginResponse registro(RegistroRequest request) {
        String normalizedCorreo = request.getCorreo().trim().toLowerCase();
        String normalizedNombreUsuario = request.getNombreUsuario().trim();
        String normalizedNombreCompleto = request.getNombreCompleto().trim();
        String normalizedNombreEmpresa = request.getNombreEmpresa().trim();

        long totalUsuarios = userRepository.count();
        if (totalUsuarios > 0) {
            throw new Exceptions.ForbiddenException(
                "Registro público no permitido. Ya existen " + totalUsuarios + " usuarios en el sistema. Usa /login.");
        }
        if (userRepository.existsByCorreoIgnoreCase(normalizedCorreo)) {
            throw new Exceptions.DuplicateResourceException("El correo ya está en uso");
        }
        if (userRepository.existsByNombreUsuarioIgnoreCase(normalizedNombreUsuario)) {
            throw new Exceptions.DuplicateResourceException("El nombre de usuario ya está en uso");
        }

        // Crear departamento ADM si no existe y asignar al primer usuario
        Department admDept = departmentService.createAdmDepartmentIfAbsent();

        User user = User.builder()
                .nombreCompleto(normalizedNombreCompleto)
                .correo(normalizedCorreo)
                .nombreUsuario(normalizedNombreUsuario)
                .hashContrasena(passwordEncoder.encode(request.getContrasena()))
                .rol(UserRole.ADM_DISENADOR)
                .idDepartamento(admDept.getId())
                .nombreEmpresa(normalizedNombreEmpresa)
                .activo(true)
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateTokenWithDept(user, admDept.getCodigo(), admDept.getNombre());
        saveSession(user, token);

        return LoginResponse.builder()
                .token(token)
                .correo(user.getCorreo())
                .nombreCompleto(user.getNombreCompleto())
                .rol(user.getRol().name())
                .idDepartamento(admDept.getId())
                .departamentoCodigo(admDept.getCodigo())
                .departamentoNombre(admDept.getNombre())
                .userId(user.getId())
                .nombreEmpresa(user.getNombreEmpresa())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedIdentifier = request.getCorreo() == null ? "" : request.getCorreo().trim();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedIdentifier, request.getContrasena()));
        } catch (DisabledException ex) {
            throw new Exceptions.ForbiddenException("Usuario desactivado. Contacta al administrador.");
        } catch (AuthenticationException ex) {
            throw new Exceptions.UnauthorizedException("Correo o contraseña incorrectos");
        }

        User user = userRepository.findByCorreoIgnoreCase(normalizedIdentifier)
            .or(() -> userRepository.findByNombreUsuarioIgnoreCase(normalizedIdentifier))
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado"));

        // Resolver código y nombre del departamento para el token y la respuesta
        String departamentoCodigo = null;
        String departamentoNombre = null;
        if (user.getIdDepartamento() != null) {
            try {
                var dept = departmentService.findById(user.getIdDepartamento());
                departamentoCodigo = dept.getCodigo();
                departamentoNombre = dept.getNombre();
            } catch (Exception ignored) { }
        }

        String token = jwtService.generateTokenWithDept(user, departamentoCodigo, departamentoNombre);
        saveSession(user, token);

        return LoginResponse.builder()
                .token(token)
                .correo(user.getCorreo())
                .nombreCompleto(user.getNombreCompleto())
                .rol(user.getRol() != null ? user.getRol().name() : null)
                .idDepartamento(user.getIdDepartamento())
                .departamentoCodigo(departamentoCodigo)
                .departamentoNombre(departamentoNombre)
                .userId(user.getId())
                .nombreEmpresa(user.getNombreEmpresa())
                .build();
    }

    public void logout(String token) {
        sessionRepository.findByTokenAcceso(token)
                .ifPresent(sessionRepository::delete);
    }

    private void saveSession(User user, String token) {
        sessionRepository.findByTokenAcceso(token)
                .ifPresent(sessionRepository::delete);

        Session session = Session.builder()
                .idUsuario(user.getId())
                .tokenAcceso(token)
                .expiraEn(Instant.now().plusMillis(86400000))
                .build();
        sessionRepository.save(session);
    }
}
