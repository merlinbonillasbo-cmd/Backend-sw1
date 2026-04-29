package com.workflow.modules.users.service;

import com.workflow.common.Exceptions;
import com.workflow.modules.departments.repository.DepartmentRepository;
import com.workflow.modules.users.dto.UserRequest;
import com.workflow.modules.users.dto.UserResponse;
import com.workflow.modules.users.model.User;
import com.workflow.modules.users.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        return userRepository.findByCorreoIgnoreCase(normalizedIdentifier)
                .or(() -> userRepository.findByNombreUsuarioIgnoreCase(normalizedIdentifier))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + normalizedIdentifier));
    }

    public UserResponse create(UserRequest request) {
        String normalizedCorreo = normalizeCorreo(request.getCorreo());
        String normalizedNombreUsuario = normalizeNombreUsuario(request.getNombreUsuario());

        if (userRepository.existsByCorreoIgnoreCase(normalizedCorreo)) {
            throw new Exceptions.DuplicateResourceException("Correo ya en uso: " + normalizedCorreo);
        }
        if (userRepository.existsByNombreUsuarioIgnoreCase(normalizedNombreUsuario)) {
            throw new Exceptions.DuplicateResourceException("Nombre de usuario ya en uso: " + normalizedNombreUsuario);
        }
        String idDepartamento = normalizeDepartamento(request.getIdDepartamento());
        resolveRolFromDepartamento(idDepartamento); // valida que exista y tenga rol

        User user = User.builder()
                .nombreUsuario(normalizedNombreUsuario)
                .correo(normalizedCorreo)
                .hashContrasena(passwordEncoder.encode(request.getContrasena()))
                .nombreCompleto(request.getNombreCompleto().trim())
                .rol(resolveRolFromDepartamento(idDepartamento))
                .idDepartamento(idDepartamento)
                .activo(true)
                .build();
        return toResponse(userRepository.save(user));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse findById(String id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    public UserResponse update(String id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado: " + id));
        String normalizedCorreo = normalizeCorreo(request.getCorreo());
        String normalizedNombreUsuario = normalizeNombreUsuario(request.getNombreUsuario());

        if (!user.getCorreo().equalsIgnoreCase(normalizedCorreo)
                && userRepository.existsByCorreoIgnoreCase(normalizedCorreo)) {
            throw new Exceptions.DuplicateResourceException("Correo ya en uso: " + normalizedCorreo);
        }
        if (!user.getNombreUsuario().equalsIgnoreCase(normalizedNombreUsuario)
            && userRepository.existsByNombreUsuarioIgnoreCase(normalizedNombreUsuario)) {
            throw new Exceptions.DuplicateResourceException("Nombre de usuario ya en uso: " + normalizedNombreUsuario);
        }

        user.setNombreCompleto(request.getNombreCompleto().trim());
        user.setCorreo(normalizedCorreo);
        user.setNombreUsuario(normalizedNombreUsuario);
        String idDep = normalizeDepartamento(request.getIdDepartamento());
        user.setIdDepartamento(idDep);
        user.setRol(resolveRolFromDepartamento(idDep));
        if (request.getContrasena() != null && !request.getContrasena().isBlank()) {
            user.setHashContrasena(passwordEncoder.encode(request.getContrasena()));
        }
        return toResponse(userRepository.save(user));
    }

    public UserResponse createByAdmin(UserRequest request) {
        String normalizedCorreo = normalizeCorreo(request.getCorreo());
        String normalizedNombreUsuario = normalizeNombreUsuario(request.getNombreUsuario());

        if (userRepository.existsByCorreoIgnoreCase(normalizedCorreo)) {
            throw new Exceptions.DuplicateResourceException("Correo ya en uso: " + normalizedCorreo);
        }
        if (userRepository.existsByNombreUsuarioIgnoreCase(normalizedNombreUsuario)) {
            throw new Exceptions.DuplicateResourceException("Nombre de usuario ya en uso: " + normalizedNombreUsuario);
        }
        // Heredar nombreEmpresa del admin autenticado
        String correoAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        String nombreEmpresa = userRepository.findByCorreoIgnoreCase(correoAdmin)
                .map(User::getNombreEmpresa)
                .orElse(null);

        String idDepartamento = normalizeDepartamento(request.getIdDepartamento());
        User user = User.builder()
                .nombreUsuario(normalizedNombreUsuario)
                .correo(normalizedCorreo)
                .hashContrasena(passwordEncoder.encode(request.getContrasena()))
                .nombreCompleto(request.getNombreCompleto().trim())
                .rol(resolveRolFromDepartamento(idDepartamento))
                .idDepartamento(idDepartamento)
                .nombreEmpresa(nombreEmpresa)
                .activo(true)
                .build();
        return toResponse(userRepository.save(user));
    }

    public void delete(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado: " + id));
        user.setActivo(false);
        userRepository.save(user);
    }

    public void reactivate(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado: " + id));
        user.setActivo(true);
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        String nombreDepartamento = null;
        String codigoDepartamento = null;
        if (user.getIdDepartamento() != null) {
            var dept = departmentRepository.findById(user.getIdDepartamento()).orElse(null);
            if (dept != null) {
                nombreDepartamento = dept.getNombre();
                codigoDepartamento = dept.getCodigo();
            }
        }
        return UserResponse.builder()
                .id(user.getId())
                .nombreUsuario(user.getNombreUsuario())
                .correo(user.getCorreo())
                .nombreCompleto(user.getNombreCompleto())
                .rol(user.getRol())
                .idDepartamento(user.getIdDepartamento())
                .nombreDepartamento(nombreDepartamento)
                .codigoDepartamento(codigoDepartamento)
                .nombreEmpresa(user.getNombreEmpresa())
                .activo(user.isActivo())
                .build();
    }

    private com.workflow.model.enums.UserRole resolveRolFromDepartamento(String idDepartamento) {
        if (idDepartamento == null || idDepartamento.isBlank()) {
            throw new Exceptions.BadRequestException("El departamento es obligatorio para resolver el rol del usuario.");
        }
        return departmentRepository.findById(idDepartamento)
                .map(d -> {
                    if (d.getRolAsignado() == null) {
                        throw new Exceptions.BadRequestException("El departamento no tiene un rol asignado: " + idDepartamento);
                    }
                    return d.getRolAsignado();
                })
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Departamento no encontrado: " + idDepartamento));
    }

    private String normalizeCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    private String normalizeNombreUsuario(String nombreUsuario) {
        return nombreUsuario == null ? "" : nombreUsuario.trim();
    }

    private String normalizeDepartamento(String idDepartamento) {
        if (idDepartamento == null) {
            return null;
        }
        String normalized = idDepartamento.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
