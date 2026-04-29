// modules/tasks/repository/ActiveTaskRepository.java
package com.workflow.modules.tasks.repository;

import com.workflow.modules.tasks.model.ActiveTask;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActiveTaskRepository extends MongoRepository<ActiveTask, String> {

    List<ActiveTask> findByIdInstancia(String idInstancia);

    List<ActiveTask> findByIdDepartamentoAsignado(String idDepartamentoAsignado);

    List<ActiveTask> findByIdUsuarioAsignado(String idUsuarioAsignado);
}
