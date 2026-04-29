// modules/collaborative/repository/CollaborativeSessionRepository.java
package com.workflow.modules.collaborative.repository;

import com.workflow.modules.collaborative.model.CollaborativeSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CollaborativeSessionRepository extends MongoRepository<CollaborativeSession, String> {

    Optional<CollaborativeSession> findByIdPolitica(String idPolitica);
}
