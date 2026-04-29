// NotificacionService.java
package com.workflow.modules.notifications.service;

import com.workflow.common.Exceptions;
import com.workflow.modules.notifications.model.Notification;
import com.workflow.modules.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificationRepository repository;

    public Page<Notification> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Notification findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Notificación no encontrada: " + id));
    }

    public List<Notification> findByUsuario(String idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public List<Notification> findNoLeidasByUsuario(String idUsuario) {
        return repository.findByIdUsuarioAndLeido(idUsuario, false);
    }

    public long countNoLeidas(String idUsuario) {
        return repository.countByIdUsuarioAndLeido(idUsuario, false);
    }

    public Notification create(Notification notification) {
        return repository.save(notification);
    }

    public Notification update(String id, Notification updated) {
        Notification existing = findById(id);
        existing.setTitulo(updated.getTitulo());
        existing.setMensaje(updated.getMensaje());
        existing.setTipo(updated.getTipo());
        existing.setLeido(updated.isLeido());
        return repository.save(existing);
    }

    public Notification patch(String id, Notification partial) {
        Notification existing = findById(id);
        if (partial.getTitulo() != null) existing.setTitulo(partial.getTitulo());
        if (partial.getMensaje() != null) existing.setMensaje(partial.getMensaje());
        if (partial.getTipo() != null) existing.setTipo(partial.getTipo());
        return repository.save(existing);
    }

    public Notification marcarComoLeida(String id) {
        Notification existing = findById(id);
        existing.setLeido(true);
        return repository.save(existing);
    }

    public void delete(String id) {
        if (!repository.existsById(id))
            throw new Exceptions.ResourceNotFoundException("Notificación no encontrada: " + id);
        repository.deleteById(id);
    }
}
