package com.workflow.modules.workflowdefinition.model;

import com.workflow.modules.forms.model.FormContract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    private String id;
    private String type;
    private String label;
    private String laneId;
    private String idDepartamento;
    private String idDepartamentoDestino;
    private List<String> idDepartamentosDestino;
    private String idUsuarioAsignado;
    private FormContract formSchema;
    private boolean crearUsuarioCliente;
    private boolean inicioFlujo;
    private double timeoutHours;
    private double x;
    private double y;
}
