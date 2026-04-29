// modules/tasks/model/TaskHistory.java
package com.workflow.modules.tasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "historial_tareas")
public class TaskHistory {

    @Id
    private String id;

    private String idInstancia;

    private String idPolitica;

    private String idNodo;

    private String idDepartamento;

    private String idUsuario;

    private String nombreUsuario;

    private String etiquetaNodo;

    // Campos de archivo inspirados en SAFlowNodeInstance.archiveDate y durationMs
    private Instant fechaArchivo;

    private long duracionMs;

    private boolean fueRetrasado;

    // Datos del formulario enviado en el nodo (contrato → verdad del proceso)
    private java.util.Map<String, Object> datos;

    @CreatedDate
    private Instant fechaCreacion;
}
