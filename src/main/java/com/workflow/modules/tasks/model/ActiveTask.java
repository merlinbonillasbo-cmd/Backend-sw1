// modules/tasks/model/ActiveTask.java
package com.workflow.modules.tasks.model;

import com.workflow.model.enums.TaskPriority;
import com.workflow.model.enums.TrafficLight;
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
@Document(collection = "tareas_activas")
public class ActiveTask {

    @Id
    private String id;

    private String idInstancia;

    private String idNodo;

    private String idDepartamentoAsignado;

    private String idUsuarioAsignado;

    private String nombreUsuario;

    private TrafficLight semaforo;

    private TaskPriority prioridad;

    private Instant fechaVencimiento;

    // Momento en que el funcionario reclamó la tarea (ROJO→AMARILLO)
    private Instant fechaInicio;

    // Referencia a la política para cálculos de timeout
    private String idPolitica;

    // Datos del formulario ingresados por el funcionario
    private java.util.Map<String, Object> datos;

    @CreatedDate
    private Instant fechaCreacion;
}
