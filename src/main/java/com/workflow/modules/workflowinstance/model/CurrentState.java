package com.workflow.modules.workflowinstance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentState {

    private String nodeId;
    private String nodeLabel;
    private String assigneeId;
    private String status;
    private Instant enteredAt;

}
