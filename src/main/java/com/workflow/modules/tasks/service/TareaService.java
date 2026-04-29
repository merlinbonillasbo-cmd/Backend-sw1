// TareaService.java
package com.workflow.modules.tasks.service;

import com.workflow.common.Exceptions;
import com.workflow.modules.tasks.model.ActiveTask;
import com.workflow.modules.tasks.repository.ActiveTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final ActiveTaskRepository repository;

    public Page<ActiveTask> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public ActiveTask findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Tarea no encontrada: " + id));
    }

    public List<ActiveTask> findByDepartamento(String idDepartamento) {
        return repository.findByIdDepartamentoAsignado(idDepartamento);
    }

    public List<ActiveTask> findByInstancia(String idInstancia) {
        return repository.findByIdInstancia(idInstancia);
    }

    public List<ActiveTask> findByUsuario(String idUsuario) {
        return repository.findByIdUsuarioAsignado(idUsuario);
    }

    public ActiveTask create(ActiveTask tarea) {
        return repository.save(tarea);
    }

    public ActiveTask update(String id, ActiveTask updated) {
        ActiveTask existing = findById(id);
        existing.setIdDepartamentoAsignado(updated.getIdDepartamentoAsignado());
        existing.setIdUsuarioAsignado(updated.getIdUsuarioAsignado());
        existing.setSemaforo(updated.getSemaforo());
        existing.setPrioridad(updated.getPrioridad());
        existing.setFechaVencimiento(updated.getFechaVencimiento());
        existing.setDatos(updated.getDatos());
        return repository.save(existing);
    }

    public ActiveTask patch(String id, ActiveTask partial) {
        ActiveTask existing = findById(id);
        if (partial.getIdUsuarioAsignado() != null) existing.setIdUsuarioAsignado(partial.getIdUsuarioAsignado());
        if (partial.getSemaforo() != null) existing.setSemaforo(partial.getSemaforo());
        if (partial.getPrioridad() != null) existing.setPrioridad(partial.getPrioridad());
        if (partial.getFechaVencimiento() != null) existing.setFechaVencimiento(partial.getFechaVencimiento());
        if (partial.getDatos() != null) existing.setDatos(partial.getDatos());
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id))
            throw new Exceptions.ResourceNotFoundException("Tarea no encontrada: " + id);
        repository.deleteById(id);
    }
}
