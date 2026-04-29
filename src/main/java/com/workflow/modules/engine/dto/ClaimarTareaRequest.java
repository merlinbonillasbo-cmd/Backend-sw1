// modules/engine/dto/ClaimarTareaRequest.java
package com.workflow.modules.engine.dto;

import lombok.Data;

/** Cuerpo del request para que un funcionario reclame una tarea (ROJO→AMARILLO) */
@Data
public class ClaimarTareaRequest {
    private String tareaId;
    private String usuarioId;
}
