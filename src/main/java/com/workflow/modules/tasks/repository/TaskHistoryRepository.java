// modules/tasks/repository/TaskHistoryRepository.java
package com.workflow.modules.tasks.repository;

import com.workflow.modules.tasks.model.TaskHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskHistoryRepository extends MongoRepository<TaskHistory, String> {

    List<TaskHistory> findByIdInstancia(String idInstancia);

    List<TaskHistory> findByIdPolitica(String idPolitica);

    List<TaskHistory> findByIdDepartamento(String idDepartamento);
}
