package com.workflow.modules.workflowdefinition.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lane {

    private String id;
    private String label;
    private String departmentId;
    private String color;

}
