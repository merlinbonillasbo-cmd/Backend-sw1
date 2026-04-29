// AuditLogRepository.java
package com.workflow.modules.history.repository;

import com.workflow.modules.history.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByProcessInstanceId(String processInstanceId);
    List<AuditLog> findByWorkflowDefinitionId(String workflowDefinitionId);
    List<AuditLog> findByActorId(String actorId);
    List<AuditLog> findByNodeId(String nodeId);
}
