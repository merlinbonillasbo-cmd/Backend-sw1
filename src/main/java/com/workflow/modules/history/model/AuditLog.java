package com.workflow.modules.history.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    private String processInstanceId;

    private String workflowDefinitionId;

    private String nodeId;

    private String nodeLabel;

    private String actorId;

    private String action;

    private String comment;

    private Map<String, Object> formData;

    @CreatedDate
    private Instant timestamp;

}
