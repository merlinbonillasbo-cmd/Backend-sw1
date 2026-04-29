package com.workflow.modules.users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.workflow.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;
    private String nombreUsuario;
    private String correo;
    private String nombreCompleto;
    /** Rol heredado del departamento (resuelto dinámicamente) */
    private UserRole rol;
    private String idDepartamento;
    private String nombreDepartamento;
    private String codigoDepartamento;
    private String nombreEmpresa;
    private boolean activo;
}
