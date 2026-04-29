// modules/analytics/dto/DeptAnalyticsDTO.java
package com.workflow.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado del aggregation pipeline sobre historial_tareas.
 * Captura el promedio de duración por nodo (patrón cuello de botella BonitaSoft).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeptAnalyticsDTO {

    /** ID del nodo del diagrama ($group._id) */
    private String idNodo;

    /** Promedio de duración en milisegundos ($avg: "$duracionMs") */
    private double duracionPromedioMs;

    /** Total de tareas completadas en ese nodo */
    private long totalTareas;

    /** Tareas que superaron el timeout ($sum: "$fueRetrasado") */
    private long tareasRetrasadas;

    /** Porcentaje de tareas retrasadas (calculado en el servicio) */
    private double porcentajeRetraso;
}
