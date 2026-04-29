// SnapshotService.java
package com.workflow.modules.snapshots.service;

import com.workflow.common.Exceptions;
import com.workflow.modules.snapshots.model.DiagramSnapshot;
import com.workflow.modules.snapshots.repository.DiagramSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final DiagramSnapshotRepository repository;

    public Page<DiagramSnapshot> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public DiagramSnapshot findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Snapshot no encontrado: " + id));
    }

    public List<DiagramSnapshot> findByPolitica(String idPolitica) {
        return repository.findByIdPoliticaOrderByVersionDesc(idPolitica);
    }

    public DiagramSnapshot create(DiagramSnapshot snapshot) {
        return repository.save(snapshot);
    }

    public DiagramSnapshot update(String id, DiagramSnapshot updated) {
        DiagramSnapshot existing = findById(id);
        existing.setDatosSnapshot(updated.getDatosSnapshot());
        existing.setGuardadoPor(updated.getGuardadoPor());
        return repository.save(existing);
    }

    public DiagramSnapshot patch(String id, DiagramSnapshot partial) {
        DiagramSnapshot existing = findById(id);
        if (partial.getDatosSnapshot() != null) existing.setDatosSnapshot(partial.getDatosSnapshot());
        if (partial.getGuardadoPor() != null) existing.setGuardadoPor(partial.getGuardadoPor());
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id))
            throw new Exceptions.ResourceNotFoundException("Snapshot no encontrado: " + id);
        repository.deleteById(id);
    }
}
