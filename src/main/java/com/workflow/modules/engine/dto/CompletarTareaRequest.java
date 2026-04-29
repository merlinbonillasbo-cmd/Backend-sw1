// modules/engine/dto/CompletarTareaRequest.java
package com.workflow.modules.engine.dto;

import lombok.Data;

import java.util.Map;

/** Cuerpo del request para completar una tarea activa */
@Data
public class CompletarTareaRequest {
    private String tareaId;
    private Map<String, Object> formData;
}
