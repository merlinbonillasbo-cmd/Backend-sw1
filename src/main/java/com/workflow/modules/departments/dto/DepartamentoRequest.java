package com.workflow.modules.departments.dto;

import com.workflow.model.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartamentoRequest {

    @NotBlank(message = "El código es requerido")
    private String codigo;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    private String descripcion;

    private String color;

    @NotNull(message = "El rol del departamento es requerido")
    private UserRole rolAsignado;
}
