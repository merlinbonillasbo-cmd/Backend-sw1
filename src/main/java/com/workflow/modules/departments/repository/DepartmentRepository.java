package com.workflow.modules.departments.repository;

import com.workflow.modules.departments.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends MongoRepository<Department, String> {

    Optional<Department> findByCodigo(String codigo);

    Optional<Department> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<Department> findByActivo(boolean activo);
}
