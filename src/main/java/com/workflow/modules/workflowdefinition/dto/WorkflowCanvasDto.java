package com.workflow.modules.workflowdefinition.dto;

import com.workflow.model.enums.PolicyStatus;
import com.workflow.modules.workflowdefinition.model.Connection;
import com.workflow.modules.workflowdefinition.model.Lane;
import com.workflow.modules.workflowdefinition.model.Node;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCanvasDto {

    private String id;
    private String titulo;
    private String descripcion;
    private String slug;
    private String xmlBpmn;
    private PolicyStatus estado;
    private String idPropietario;
    private List<String> colaboradores;
    private List<Lane> carriles;
    private List<Node> nodos;
    private List<Connection> conexiones;
    private List<String> etiquetas;
}
