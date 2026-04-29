package com.workflow.modules.departments.controller;

import com.workflow.common.ApiResponse;
import com.workflow.modules.departments.dto.DepartamentoRequest;
import com.workflow.modules.departments.model.Department;
import com.workflow.modules.departments.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Department>> create(@Valid @RequestBody DepartamentoRequest department) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Department created", departmentService.create(department)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Department>> update(@PathVariable String id,
                                                          @Valid @RequestBody DepartamentoRequest department) {
        return ResponseEntity.ok(ApiResponse.ok("Department updated", departmentService.update(id, department)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_DISENADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Department deleted", null));
    }

}
