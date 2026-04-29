package com.workflow.modules.workflowdefinition.repository;

import com.workflow.model.enums.PolicyStatus;
import com.workflow.modules.workflowdefinition.model.WorkflowDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowDefinitionRepository extends MongoRepository<WorkflowDefinition, String> {

    Optional<WorkflowDefinition> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<WorkflowDefinition> findByEstado(PolicyStatus estado);

    List<WorkflowDefinition> findByIdPropietario(String idPropietario);

    List<WorkflowDefinition> findByColaboradoresContaining(String idUsuario);
}
