package com.workflow.modules.workflowinstance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrearClienteRequest {

    @NotBlank(message = "La contrasena es requerida")
    private String contrasena;

    // Fallback opcional si no viene en los datos del flujo
    private String nombreCompleto;
    private String telefono;
    private String direccion;
}
