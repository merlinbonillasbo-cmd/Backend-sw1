// RolService.java
package com.workflow.modules.roles.service;

import com.workflow.common.Exceptions;
import com.workflow.modules.roles.model.RolePermission;
import com.workflow.modules.roles.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolePermissionRepository repository;

    public Page<RolePermission> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public RolePermission findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Rol no encontrado: " + id));
    }

    public RolePermission findByNombreRol(String nombreRol) {
        return repository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Rol no encontrado: " + nombreRol));
    }

    public RolePermission create(RolePermission rol) {
        if (repository.existsByNombreRol(rol.getNombreRol()))
            throw new Exceptions.DuplicateResourceException("Rol ya existe: " + rol.getNombreRol());
        return repository.save(rol);
    }

    public RolePermission update(String id, RolePermission updated) {
        RolePermission existing = findById(id);
        existing.setNombreRol(updated.getNombreRol());
        existing.setPermisos(updated.getPermisos());
        existing.setEsRolSistema(updated.isEsRolSistema());
        return repository.save(existing);
    }

    public RolePermission patch(String id, RolePermission partial) {
        RolePermission existing = findById(id);
        if (partial.getNombreRol() != null) existing.setNombreRol(partial.getNombreRol());
        if (partial.getPermisos() != null) existing.setPermisos(partial.getPermisos());
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id))
            throw new Exceptions.ResourceNotFoundException("Rol no encontrado: " + id);
        repository.deleteById(id);
    }
}
