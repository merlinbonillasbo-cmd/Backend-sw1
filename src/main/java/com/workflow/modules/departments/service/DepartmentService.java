package com.workflow.modules.departments.service;

import com.workflow.common.Exceptions;
import com.workflow.model.enums.UserRole;
import com.workflow.modules.departments.dto.DepartamentoRequest;
import com.workflow.modules.departments.model.Department;
import com.workflow.modules.departments.repository.DepartmentRepository;
import com.workflow.modules.users.model.User;
import com.workflow.modules.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private static final String CODIGO_ADM = "ADM";

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public Department create(DepartamentoRequest request) {
        String normalizedCodigo = normalizeCodigo(request.getCodigo());

        if (departmentRepository.existsByCodigoIgnoreCase(normalizedCodigo)) {
            throw new Exceptions.DuplicateResourceException("Departamento ya existe: " + normalizedCodigo);
        }
        Department dept = Department.builder()
                .codigo(normalizedCodigo)
                .nombre(request.getNombre().trim())
                .descripcion(normalizeDescripcion(request.getDescripcion()))
                .color(normalizeColor(request.getColor()))
                .rolAsignado(request.getRolAsignado())
                .activo(true)
                .build();
        return departmentRepository.save(dept);
    }

    /** Crea el departamento ADM_DISEÑADOR inicial (solo si no existe). */
    public Department createAdmDepartmentIfAbsent() {
        return departmentRepository.findByCodigoIgnoreCase(CODIGO_ADM).orElseGet(() -> {
            Department adm = Department.builder()
                    .codigo(CODIGO_ADM)
                    .nombre("Administración y Diseño")
                    .descripcion("Departamento del administrador del sistema")
                    .rolAsignado(UserRole.ADM_DISENADOR)
                    .activo(true)
                    .build();
            return departmentRepository.save(adm);
        });
    }

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Department findById(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Departamento no encontrado: " + id));
    }

    public List<User> findUsuariosByDepartamento(String id) {
        findById(id); // valida existencia
        return userRepository.findByIdDepartamento(id).stream()
                .filter(User::isActivo)
                .toList();
    }

    public Department update(String id, DepartamentoRequest request) {
        Department existing = findById(id);
        String normalizedCodigo = normalizeCodigo(request.getCodigo());

        if (!existing.getCodigo().equalsIgnoreCase(normalizedCodigo)
                && departmentRepository.existsByCodigoIgnoreCase(normalizedCodigo)) {
            throw new Exceptions.DuplicateResourceException("Departamento ya existe: " + normalizedCodigo);
        }

        existing.setCodigo(normalizedCodigo);
        existing.setNombre(request.getNombre().trim());
        existing.setDescripcion(normalizeDescripcion(request.getDescripcion()));
        existing.setColor(normalizeColor(request.getColor()));
        // No permite cambiar el rolAsignado del departamento ADM
        if (!CODIGO_ADM.equalsIgnoreCase(existing.getCodigo())) {
            existing.setRolAsignado(request.getRolAsignado());
        }
        return departmentRepository.save(existing);
    }

    public void delete(String id) {
        Department existing = findById(id);
        if (CODIGO_ADM.equalsIgnoreCase(existing.getCodigo())) {
            throw new Exceptions.ForbiddenException("El departamento ADM no puede eliminarse.");
        }
        existing.setActivo(false);
        departmentRepository.save(existing);
    }

    private String normalizeCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private String normalizeDescripcion(String descripcion) {
        if (descripcion == null) {
            return null;
        }
        String normalized = descripcion.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeColor(String color) {
        if (color == null) {
            return "#0ea5e9";
        }
        String normalized = color.trim();
        return normalized.isEmpty() ? "#0ea5e9" : normalized;
    }
}
