// modules/analytics/dto/RendimientoDeptDTO.java
package com.workflow.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Estadísticas de rendimiento por departamento, calculadas sobre historial_tareas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendimientoDeptDTO {

    /** ID del departamento ($group._id) */
    @Field("_id")
    private String idDepartamento;

    /** Total de tareas completadas */
    private long totalTareas;

    /** Tareas que superaron el timeout */
    private long tareasRetrasadas;

    /** Porcentaje de tareas retrasadas (calculado en el servicio) */
    private double porcentajeRetraso;

    /** Duración promedio en milisegundos */
    private double duracionPromedioMs;
}
