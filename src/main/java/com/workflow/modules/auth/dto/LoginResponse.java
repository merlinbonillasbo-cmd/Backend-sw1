package com.workflow.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String userId;
    private String correo;
    private String nombreCompleto;
    private String rol;
    /** ID interno del departamento (para consultas de tareas) */
    private String idDepartamento;
    /** Código del departamento del usuario (para routing en frontend) */
    private String departamentoCodigo;
    /** Nombre completo del departamento (para mostrar en UI) */
    private String departamentoNombre;
    private String nombreEmpresa;
}
