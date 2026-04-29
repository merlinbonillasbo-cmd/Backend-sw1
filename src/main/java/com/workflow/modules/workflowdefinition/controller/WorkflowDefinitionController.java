package com.workflow.modules.workflowdefinition.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.workflowdefinition.dto.WorkflowCanvasDto;
import com.workflow.modules.workflowdefinition.model.WorkflowDefinition;
import com.workflow.modules.workflowdefinition.service.WorkflowDefinitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow-definitions")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService service;

    public WorkflowDefinitionController(WorkflowDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> create(@RequestBody WorkflowCanvasDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Workflow definition created", service.create(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> update(@PathVariable String id,
                                                                   @RequestBody WorkflowCanvasDto dto) {
        return ResponseEntity.ok(ApiResponse.ok("Workflow definition updated", service.update(id, dto)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> publish(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Workflow definition published", service.publish(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Workflow definition deleted", null));
    }

}
