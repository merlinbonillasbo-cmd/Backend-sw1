package com.workflow.modules.workflowinstance.model;

import com.workflow.model.embedded.SearchIndex;
import com.workflow.model.enums.InstanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "instancias_proceso")
public class ProcessInstance {

    @Id
    private String id;

    private String idPolitica;

    private String idCliente;

    private String idDepartamentoActual;

    // Nodo actual en ejecución (patrón SAFlowNodeInstance.stateId)
    private String nodoActualId;

    private InstanceStatus estado;

    // Datos acumulados del proceso para trazabilidad y vista del cliente
    private java.util.Map<String, Object> datosProceso;

    // Motivo de rechazo cuando el flujo termina en estado CANCELADO
    private String motivoRechazo;

    private SearchIndex indiceBusqueda;

    @CreatedDate
    private Instant fechaCreacion;

    @LastModifiedDate
    private Instant fechaActualizacion;
}
