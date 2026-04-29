package com.workflow.modules.workflowinstance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrearClienteResponse {
    private String idCliente;
    private String nombreCompleto;
    private String correo;
    private String nombreUsuario;
}
