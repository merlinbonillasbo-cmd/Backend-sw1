// modules/analytics/dto/CuelloDeBotellaDTO.java
package com.workflow.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un nodo identificado como cuello de botella.
 * Top-5 nodos con mayor duracionPromedioMs en todo el historial.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuelloDeBotellaDTO {

    private String idNodo;

    private String etiquetaNodo;

    private double duracionPromedioMs;

    private long totalTareas;

    private long tareasRetrasadas;
}
