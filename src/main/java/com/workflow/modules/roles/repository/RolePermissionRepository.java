// modules/roles/repository/RolePermissionRepository.java
package com.workflow.modules.roles.repository;

import com.workflow.modules.roles.model.RolePermission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RolePermissionRepository extends MongoRepository<RolePermission, String> {

    Optional<RolePermission> findByNombreRol(String nombreRol);

    boolean existsByNombreRol(String nombreRol);
}
