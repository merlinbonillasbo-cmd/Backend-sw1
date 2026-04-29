package com.workflow.modules.workflowdefinition.service;

import com.workflow.common.Exceptions;
import com.workflow.model.enums.PolicyStatus;
import com.workflow.modules.workflowdefinition.dto.WorkflowCanvasDto;
import com.workflow.modules.workflowdefinition.model.WorkflowDefinition;
import com.workflow.modules.workflowdefinition.repository.WorkflowDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowDefinitionService {

    private final WorkflowDefinitionRepository repository;

    public WorkflowDefinitionService(WorkflowDefinitionRepository repository) {
        this.repository = repository;
    }

    public WorkflowDefinition create(WorkflowCanvasDto dto) {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .slug(dto.getSlug())
                .xmlBpmn(dto.getXmlBpmn())
                .estado(PolicyStatus.BORRADOR)
                .idPropietario(dto.getIdPropietario())
                .colaboradores(dto.getColaboradores())
                .carriles(dto.getCarriles())
                .nodos(dto.getNodos())
                .conexiones(dto.getConexiones())
                .etiquetas(dto.getEtiquetas())
                .build();
        return repository.save(definition);
    }

    public List<WorkflowDefinition> findAll() {
        return repository.findAll();
    }

    public WorkflowDefinition findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Definición no encontrada: " + id));
    }

    public WorkflowDefinition update(String id, WorkflowCanvasDto dto) {
        WorkflowDefinition existing = findById(id);
        existing.setTitulo(dto.getTitulo());
        existing.setDescripcion(dto.getDescripcion());
        existing.setXmlBpmn(dto.getXmlBpmn());
        existing.setCarriles(dto.getCarriles());
        existing.setNodos(dto.getNodos());
        existing.setConexiones(dto.getConexiones());
        if (dto.getColaboradores() != null) {
            existing.setColaboradores(dto.getColaboradores());
        }
        return repository.save(existing);
    }

    public WorkflowDefinition publish(String id) {
        WorkflowDefinition definition = findById(id);
        definition.setEstado(PolicyStatus.ACTIVO);
        return repository.save(definition);
    }

    public WorkflowDefinition revertir(String id) {
        WorkflowDefinition definition = findById(id);
        definition.setEstado(PolicyStatus.BORRADOR);
        return repository.save(definition);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new Exceptions.ResourceNotFoundException("Definición no encontrada: " + id);
        }
        repository.deleteById(id);
    }
}
