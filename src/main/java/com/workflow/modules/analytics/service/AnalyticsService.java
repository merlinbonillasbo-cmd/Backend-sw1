// modules/analytics/service/AnalyticsService.java
package com.workflow.modules.analytics.service;

import com.workflow.modules.analytics.dto.CuelloDeBotellaDTO;
import com.workflow.modules.analytics.dto.DeptAnalyticsDTO;
import com.workflow.modules.analytics.dto.RendimientoDeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Analítica del motor de workflow para el Gerente.
 *
 * Usa aggregation pipelines en MongoTemplate sobre historial_tareas, inspirado en
 * el patrón de SAFlowNodeInstance donde archiveDate y durationMs son la base
 * para detectar cuellos de botella entre nodos del proceso.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;

    // ─────────────────────────────────────────────────────────────────────────
    // DURACIÓN PROMEDIO POR DEPARTAMENTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pipeline sobre historial_tareas:
     *   $match  → filtra por idDepartamento
     *   $group  → agrupa por idNodo calculando avgDurationMs, totalTareas, tareasRetrasadas
     *   $sort   → por duracionPromedioMs desc
     *
     * Patrón: el Gerente detecta qué nodo dentro de su departamento es más lento.
     *
     * @param idDepartamento ID del departamento a analizar
     */
    public List<DeptAnalyticsDTO> getDuracionPromedioPorDepartamento(String idDepartamento) {

        Aggregation agg = Aggregation.newAggregation(

            // $match: filtrar por departamento
            Aggregation.match(Criteria.where("idDepartamento").is(idDepartamento)),

            // $group: agrupar por nodo con métricas de duración
            Aggregation.group("idNodo")
                .avg("duracionMs").as("duracionPromedioMs")
                .count().as("totalTareas")
                .sum(ConditionalOperators
                        .when(Criteria.where("fueRetrasado").is(true))
                        .then(1).otherwise(0))
                .as("tareasRetrasadas"),

            // $sort: los nodos más lentos primero
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "duracionPromedioMs"))
        );

        AggregationResults<DeptAnalyticsDTO> results =
                mongoTemplate.aggregate(agg, "historial_tareas", DeptAnalyticsDTO.class);

        List<DeptAnalyticsDTO> lista = results.getMappedResults();

        // Enriquecer con porcentaje de retraso
        lista.forEach(dto -> {
            double pct = dto.getTotalTareas() > 0
                    ? (dto.getTareasRetrasadas() * 100.0) / dto.getTotalTareas()
                    : 0.0;
            dto.setPorcentajeRetraso(Math.round(pct * 100.0) / 100.0);
        });

        log.info("[Analytics] Departamento {}: {} nodos analizados", idDepartamento, lista.size());
        return lista;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CUELLOS DE BOTELLA  (Top-5 nodos más lentos de todo el sistema)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pipeline global sobre historial_tareas:
     *   $group  → agrupa por idNodo con avgDurationMs, totalTareas, tareasRetrasadas
     *   $sort   → por avgMs desc
     *   $limit  → top 5
     *
     * Devuelve los 5 nodos con mayor tiempo promedio en todo el historial.
     * Permite al Gerente identificar dónde rediseñar el proceso.
     */
    public List<CuelloDeBotellaDTO> getCuellosDeBottella() {

        Aggregation agg = Aggregation.newAggregation(

            // $group: sin filtro de departamento → todo el historial
            Aggregation.group("idNodo")
                .avg("duracionMs").as("duracionPromedioMs")
                .count().as("totalTareas")
                .sum(ConditionalOperators
                        .when(Criteria.where("fueRetrasado").is(true))
                        .then(1).otherwise(0))
                .as("tareasRetrasadas")
                .first("etiquetaNodo").as("etiquetaNodo"),

            // $sort: más lentos primero
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "duracionPromedioMs")),

            // $limit: top 5
            Aggregation.limit(5)
        );

        AggregationResults<CuelloDeBotellaDTO> results =
                mongoTemplate.aggregate(agg, "historial_tareas", CuelloDeBotellaDTO.class);

        List<CuelloDeBotellaDTO> cuellos = results.getMappedResults();

        // Asegurar que idNodo se mapea desde el campo _id del $group
        cuellos.forEach(c -> {
            if (c.getIdNodo() == null) {
                log.warn("[Analytics] CuelloDeBotellaDTO sin idNodo, revisar mapeo del pipeline.");
            }
        });

        log.info("[Analytics] Cuellos de botella identificados: {}", cuellos.size());
        return cuellos;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RENDIMIENTO POR DEPARTAMENTO  (ranking de todos los departamentos)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Agrupa historial_tareas por idDepartamento y calcula métricas.
     * Retorna todos los departamentos ordenados por totalTareas desc.
     */
    public List<RendimientoDeptDTO> getRendimientoPorDepartamentos() {

        Aggregation agg = Aggregation.newAggregation(
            Aggregation.group("idDepartamento")
                .count().as("totalTareas")
                .sum(ConditionalOperators
                        .when(Criteria.where("fueRetrasado").is(true))
                        .then(1).otherwise(0))
                .as("tareasRetrasadas")
                .avg("duracionMs").as("duracionPromedioMs"),
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalTareas"))
        );

        AggregationResults<RendimientoDeptDTO> results =
                mongoTemplate.aggregate(agg, "historial_tareas", RendimientoDeptDTO.class);

        List<RendimientoDeptDTO> lista = results.getMappedResults();

        lista.forEach(dto -> {
            double pct = dto.getTotalTareas() > 0
                    ? (dto.getTareasRetrasadas() * 100.0) / dto.getTotalTareas()
                    : 0.0;
            dto.setPorcentajeRetraso(Math.round(pct * 100.0) / 100.0);
        });

        log.info("[Analytics] Rendimiento departamentos: {} registros", lista.size());
        return lista;
    }
}
