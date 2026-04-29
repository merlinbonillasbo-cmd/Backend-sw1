// modules/snapshots/repository/DiagramSnapshotRepository.java
package com.workflow.modules.snapshots.repository;

import com.workflow.modules.snapshots.model.DiagramSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DiagramSnapshotRepository extends MongoRepository<DiagramSnapshot, String> {

    List<DiagramSnapshot> findByIdPoliticaOrderByVersionDesc(String idPolitica);

    Optional<DiagramSnapshot> findFirstByIdPoliticaOrderByVersionDesc(String idPolitica);
}
