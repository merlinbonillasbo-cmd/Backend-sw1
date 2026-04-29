// modules/engine/dto/DerivarTramiteRequest.java
package com.workflow.modules.engine.dto;

import lombok.Data;

import java.util.Map;

/** Cuerpo del request para derivar un trámite al siguiente nodo */
@Data
public class DerivarTramiteRequest {
    private String instanciaId;
    private Map<String, Object> formData;
}
