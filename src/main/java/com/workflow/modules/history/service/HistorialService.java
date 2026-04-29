// HistorialService.java
package com.workflow.modules.history.service;

import com.workflow.common.Exceptions;
import com.workflow.modules.history.model.AuditLog;
import com.workflow.modules.history.repository.AuditLogRepository;
import com.workflow.modules.tasks.model.TaskHistory;
import com.workflow.modules.tasks.repository.TaskHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialService {

    private final AuditLogRepository auditLogRepository;
    private final TaskHistoryRepository taskHistoryRepository;

    // ── AuditLog ────────────────────────────────────────────────────────────

    public Page<AuditLog> findAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    public AuditLog findAuditLogById(String id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Registro de historial no encontrado: " + id));
    }

    public List<AuditLog> findByInstancia(String processInstanceId) {
        return auditLogRepository.findByProcessInstanceId(processInstanceId);
    }

    public List<AuditLog> findByActor(String actorId) {
        return auditLogRepository.findByActorId(actorId);
    }

    public AuditLog create(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    public AuditLog update(String id, AuditLog updated) {
        AuditLog existing = findAuditLogById(id);
        existing.setAction(updated.getAction());
        existing.setComment(updated.getComment());
        existing.setFormData(updated.getFormData());
        return auditLogRepository.save(existing);
    }

    public AuditLog patch(String id, AuditLog partial) {
        AuditLog existing = findAuditLogById(id);
        if (partial.getComment() != null) existing.setComment(partial.getComment());
        if (partial.getFormData() != null) existing.setFormData(partial.getFormData());
        return auditLogRepository.save(existing);
    }

    public void delete(String id) {
        if (!auditLogRepository.existsById(id))
            throw new Exceptions.ResourceNotFoundException("Registro de historial no encontrado: " + id);
        auditLogRepository.deleteById(id);
    }

    // ── TaskHistory ─────────────────────────────────────────────────────────

    public List<TaskHistory> findTaskHistoryByInstancia(String idInstancia) {
        return taskHistoryRepository.findByIdInstancia(idInstancia);
    }

    public List<TaskHistory> findTaskHistoryByPolitica(String idPolitica) {
        return taskHistoryRepository.findByIdPolitica(idPolitica);
    }

    public List<TaskHistory> findTaskHistoryByDepartamento(String idDepartamento) {
        return taskHistoryRepository.findByIdDepartamento(idDepartamento);
    }
}
