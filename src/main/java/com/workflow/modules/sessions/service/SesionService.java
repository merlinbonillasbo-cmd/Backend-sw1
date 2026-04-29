// SesionService.java
package com.workflow.modules.sessions.service;

import com.workflow.common.Exceptions;
import com.workflow.modules.sessions.model.Session;
import com.workflow.modules.sessions.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SesionService {

    private final SessionRepository repository;

    public Page<Session> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Session findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Sesión no encontrada: " + id));
    }

    public Session create(Session session) {
        return repository.save(session);
    }

    public Session update(String id, Session updated) {
        Session existing = findById(id);
        existing.setIdUsuario(updated.getIdUsuario());
        existing.setTokenAcceso(updated.getTokenAcceso());
        existing.setTokenRefresh(updated.getTokenRefresh());
        existing.setIdSocket(updated.getIdSocket());
        existing.setExpiraEn(updated.getExpiraEn());
        return repository.save(existing);
    }

    public Session patch(String id, Session partial) {
        Session existing = findById(id);
        if (partial.getIdSocket() != null) existing.setIdSocket(partial.getIdSocket());
        if (partial.getExpiraEn() != null) existing.setExpiraEn(partial.getExpiraEn());
        if (partial.getTokenRefresh() != null) existing.setTokenRefresh(partial.getTokenRefresh());
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id))
            throw new Exceptions.ResourceNotFoundException("Sesión no encontrada: " + id);
        repository.deleteById(id);
    }

    public void deleteByUsuario(String idUsuario) {
        repository.deleteByIdUsuario(idUsuario);
    }
}
