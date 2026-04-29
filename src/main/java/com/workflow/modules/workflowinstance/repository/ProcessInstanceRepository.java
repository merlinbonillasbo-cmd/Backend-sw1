// modules/workflowinstance/repository/ProcessInstanceRepository.java
package com.workflow.modules.workflowinstance.repository;

import com.workflow.model.enums.InstanceStatus;
import com.workflow.modules.workflowinstance.model.ProcessInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProcessInstanceRepository extends MongoRepository<ProcessInstance, String> {

    Optional<ProcessInstance> findByIndiceBusquedaRut(String rut);

    Optional<ProcessInstance> findByIndiceBusquedaCodigoCaso(String codigoCaso);

    long countByFechaCreacionBetween(Instant inicio, Instant fin);

    List<ProcessInstance> findByIdPolitica(String idPolitica);

    List<ProcessInstance> findByIdCliente(String idCliente);

    List<ProcessInstance> findByIdDepartamentoActual(String idDepartamentoActual);

    List<ProcessInstance> findByEstado(InstanceStatus estado);
}
