package com.workflow.modules.workflowdefinition.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connection {

    private String id;
    private String sourceNodeId;
    private String targetNodeId;
    private String label;
    private String condition;

}
