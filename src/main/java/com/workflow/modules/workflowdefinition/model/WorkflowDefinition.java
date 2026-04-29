package com.workflow.modules.workflowdefinition.model;

import com.workflow.model.embedded.CanvasConfig;
import com.workflow.model.enums.PolicyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "definicion_politicas")
public class WorkflowDefinition {

    @Id
    private String id;

    private String titulo;

    private String descripcion;

    @Indexed(unique = true)
    private String slug;

    private String xmlBpmn;

    private PolicyStatus estado;

    private String idPropietario;

    private List<String> colaboradores;

    private List<Lane> carriles;

    private List<Node> nodos;

    private List<Connection> conexiones;

    private CanvasConfig configuracionCanvas;

    private List<String> etiquetas;

    @Version
    private Long version;

    @CreatedDate
    private Instant fechaCreacion;

    @LastModifiedDate
    private Instant fechaActualizacion;
}
