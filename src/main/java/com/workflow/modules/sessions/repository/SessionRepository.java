// modules/sessions/repository/SessionRepository.java
package com.workflow.modules.sessions.repository;

import com.workflow.modules.sessions.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SessionRepository extends MongoRepository<Session, String> {

    Optional<Session> findByTokenAcceso(String tokenAcceso);

    void deleteByIdUsuario(String idUsuario);
}
