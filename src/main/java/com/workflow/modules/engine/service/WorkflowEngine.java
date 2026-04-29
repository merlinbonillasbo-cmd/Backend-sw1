// modules/engine/service/WorkflowEngine.java
package com.workflow.modules.engine.service;

import com.workflow.common.NodeNotFoundException;
import com.workflow.common.WorkflowException;
import com.workflow.model.enums.InstanceStatus;
import com.workflow.model.enums.TrafficLight;
import com.workflow.modules.departments.repository.DepartmentRepository;
import com.workflow.modules.notifications.model.Notification;
import com.workflow.modules.notifications.repository.NotificationRepository;
import com.workflow.modules.tasks.model.ActiveTask;
import com.workflow.modules.tasks.model.TaskHistory;
import com.workflow.modules.tasks.repository.ActiveTaskRepository;
import com.workflow.modules.tasks.repository.TaskHistoryRepository;
import com.workflow.modules.workflowdefinition.model.Connection;
import com.workflow.modules.workflowdefinition.model.Node;
import com.workflow.modules.workflowdefinition.model.WorkflowDefinition;
import com.workflow.modules.workflowdefinition.repository.WorkflowDefinitionRepository;
import com.workflow.modules.workflowinstance.model.ProcessInstance;
import com.workflow.modules.workflowinstance.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Motor central de workflow.
 *
 * Algoritmo de transición de estado inspirado en FlowNodeStateManagerImpl de BonitaSoft:
 *   getNextState() → itera estados hasta encontrar uno que "shouldExecuteState"
 *   Aquí se adapta evaluando condiciones SpEL en las Connection (STransitionDefinition.getCondition()).
 *
 * Semáforo (stateId BonitaSoft → TrafficLight):
 *   stateId=4 (ROJO)    → tarea recibida, nadie la tomó
 *   stateId=1 (AMARILLO) → funcionario reclamó, está trabajando
 *   stateId=2 (VERDE)   → completada, se archiva y enruta
 *
 * archiveDate y durationMs tomados de SAFlowNodeInstance.archiveDate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final ProcessInstanceRepository instanceRepo;
    private final WorkflowDefinitionRepository policyRepo;
    private final ActiveTaskRepository activeTaskRepo;
    private final TaskHistoryRepository historyRepo;
    private final NotificationRepository notificationRepo;
    private final SimpMessagingTemplate messaging;
    private final DepartmentRepository departmentRepository;

    // Reutilizable; SpelExpressionParser es thread-safe
    private final ExpressionParser spelParser = new SpelExpressionParser();

    // ─────────────────────────────────────────────────────────────────────────
    // DERIVAR TRÁMITE  (patrón getNextState de FlowNodeStateManagerImpl)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Evalúa las conexiones salientes del nodo actual con SpEL (adaptado de
     * STransitionDefinition.getCondition() + hasCondition()) y crea la nueva
     * tarea activa en el departamento destino.
     *
     * @param instanciaId ID de la ProcessInstance a avanzar
     * @param formData    datos del formulario que sirven como variables SpEL
     */
    @Transactional
    public ActiveTask derivarTramite(String instanciaId, Map<String, Object> formData) {

        // 1. Cargar instancia
        ProcessInstance instancia = instanceRepo.findById(instanciaId)
                .orElseThrow(() -> new WorkflowException("Instancia no encontrada: " + instanciaId));

        // 2. Cargar definición de la política
        // Se extrae idPolitica a variable final para poder usarlo en lambdas
        // (instancia se reasigna más abajo en el bloque catch de OptimisticLock)
        final String idPoliticaRef = instancia.getIdPolitica();
        WorkflowDefinition politica = policyRepo.findById(idPoliticaRef)
                .orElseThrow(() -> new WorkflowException("Política no encontrada: " + idPoliticaRef));

        // 3. Datos acumulados del proceso (viajan entre tareas)
        Map<String, Object> datosProceso = mergeDatos(instancia.getDatosProceso(), formData);

        // 4. Nodo actual (equivalente a SFlowNodeInstance.stateId en Bonita)
        String nodoActualId = instancia.getNodoActualId();
        Node nodoActual = resolverNodo(politica, nodoActualId);

        // 4. Evaluar conexiones salientes → getNextState adaptado
        //    Prioridad: conexiones con condición verdadera; fallback: sin condición
        Node nodoDestino = resolverNodoDestino(politica, nodoActualId, datosProceso);

        // 5. Verificar si llegamos al END; si es así, completar la instancia
        if (esNodoFin(nodoDestino)) {
            InstanceStatus finalStatus = determinarEstadoFinal(nodoDestino);
            instancia.setEstado(finalStatus);
            instancia.setNodoActualId(nodoDestino.getId());
            instancia.setDatosProceso(datosProceso);
            if (finalStatus == InstanceStatus.CANCELADO) {
                instancia.setMotivoRechazo(extraerMotivoRechazo(datosProceso));
            }
            instanceRepo.save(instancia);
            log.info("[WorkflowEngine] Instancia {} finalizada en nodo END '{}' (estado={})",
                    instanciaId, nodoDestino.getLabel(), finalStatus);
            notificarCliente(instancia, "FIN");
            return null; // flujo terminado
        }

        // 6. Crear tarea activa con semáforo ROJO (stateId=4 en Bonita)
        Instant ahora = Instant.now();
        long timeoutMs = (long) (nodoDestino.getTimeoutHours() * 3_600_000L);

        ActiveTask nuevaTarea = ActiveTask.builder()
                .idInstancia(instanciaId)
                .idPolitica(instancia.getIdPolitica())
                .idNodo(nodoDestino.getId())
                .idDepartamentoAsignado(nodoDestino.getIdDepartamento())
                .idUsuarioAsignado(nodoDestino.getIdUsuarioAsignado())
                .semaforo(TrafficLight.ROJO)           // stateId=4: recién llegado
                .datos(datosProceso)
                .fechaVencimiento(ahora.plusMillis(timeoutMs))
                .build();
        activeTaskRepo.save(nuevaTarea);

        // 7. Actualizar instancia con el nuevo nodo y departamento actual
        try {
            instancia.setNodoActualId(nodoDestino.getId());
            instancia.setIdDepartamentoActual(nodoDestino.getIdDepartamento());
            instancia.setEstado(InstanceStatus.EN_PROCESO);
            instancia.setDatosProceso(datosProceso);
            instanceRepo.save(instancia);
        } catch (OptimisticLockingFailureException e) {
            // Conflict de edición colaborativa: recargar y reintentar una vez
            log.warn("[WorkflowEngine] OptimisticLock en instancia {}, reintentando...", instanciaId);
            instancia = instanceRepo.findById(instanciaId)
                    .orElseThrow(() -> new WorkflowException("Instancia desapareció durante reintento: " + instanciaId));
            instancia.setNodoActualId(nodoDestino.getId());
            instancia.setIdDepartamentoActual(nodoDestino.getIdDepartamento());
            instancia.setDatosProceso(datosProceso);
            instanceRepo.save(instancia);
        }

        // 8. Notificar al usuario asignado del departamento siguiente
        if (nodoDestino.getIdUsuarioAsignado() != null) {
            enviarNotificacion(
                    nodoDestino.getIdUsuarioAsignado(),
                    "Nueva tarea asignada: " + nodoDestino.getLabel(),
                    "Tienes un trámite nuevo que requiere tu atención en el nodo: " + nodoDestino.getLabel(),
                    "TAREA_ASIGNADA",
                    instanciaId,
                    nuevaTarea.getId()
            );
        }

        // 9. Notificar al departamento vía WebSocket (broker /topic)
        String deptCodigo = departmentRepository.findById(nodoDestino.getIdDepartamento())
                .map(d -> d.getCodigo())
                .orElse(nodoDestino.getIdDepartamento()); // fallback: usar el id tal cual
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("tipo", "NUEVA_TAREA");
        wsPayload.put("mensaje", "Nueva tarea: " + nodoDestino.getLabel());
        wsPayload.put("tareaId", nuevaTarea.getId());
        wsPayload.put("instanciaId", instanciaId);
        wsPayload.put("timestamp", Instant.now().toString());
        messaging.convertAndSend("/topic/notificaciones/" + deptCodigo, (Object) wsPayload);

        notificarCliente(instancia, "AVANCE");

        log.info("[WorkflowEngine] Trámite {} derivado a nodo '{}' (dept: {})",
                instanciaId, nodoDestino.getLabel(), nodoDestino.getIdDepartamento());

        return nuevaTarea;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLAIMAR TAREA  (ROJO → AMARILLO, stateId=4 → stateId=1)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * El funcionario reclama la tarea: semáforo pasa de ROJO a AMARILLO y
     * se registra quién la tomó y cuándo empezó (fechaInicio = startedAt de Bonita).
     */
    @Transactional
    public ActiveTask claimarTarea(String tareaId, String usuarioId) {
        return claimarTarea(tareaId, usuarioId, null);
    }

    public ActiveTask claimarTarea(String tareaId, String usuarioId, String nombreUsuario) {
        ActiveTask tarea = activeTaskRepo.findById(tareaId)
                .orElseThrow(() -> new WorkflowException("Tarea no encontrada: " + tareaId));

        if (tarea.getSemaforo() != TrafficLight.ROJO) {
            throw new WorkflowException("Solo se puede reclamar una tarea en estado ROJO. Estado actual: "
                    + tarea.getSemaforo());
        }

        tarea.setIdUsuarioAsignado(usuarioId);
        tarea.setNombreUsuario(nombreUsuario);
        tarea.setSemaforo(TrafficLight.AMARILLO);   // stateId=1: en progreso
        tarea.setFechaInicio(Instant.now());
        activeTaskRepo.save(tarea);

        log.info("[WorkflowEngine] Tarea {} reclamada por usuario {} (ROJO→AMARILLO)", tareaId, usuarioId);
        return tarea;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPLETAR TAREA  (AMARILLO → VERDE, stateId=1 → stateId=2)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Completa la tarea activa:
     *  1. Calcula durationMs y wasDelayed (patrón SAFlowNodeInstance)
     *  2. Archiva en historial_tareas
     *  3. Elimina de tareas_activas
     *  4. Llama a derivarTramite() para continuar el flujo
     */
    @Transactional
    public ActiveTask completarTarea(String tareaId, Map<String, Object> formData) {
        ActiveTask tarea = activeTaskRepo.findById(tareaId)
                .orElseThrow(() -> new WorkflowException("Tarea no encontrada: " + tareaId));

        if (tarea.getSemaforo() == TrafficLight.ROJO) {
            throw new WorkflowException("Debes reclamar la tarea antes de completarla (ROJO→AMARILLO→VERDE).");
        }

        // Calcular duración (SAFlowNodeInstance.archiveDate pattern)
        Instant fechaArchivo = Instant.now();
        Instant inicio = tarea.getFechaInicio() != null ? tarea.getFechaInicio() : tarea.getFechaCreacion();
        long duracionMs = fechaArchivo.toEpochMilli() - inicio.toEpochMilli();

        // Determinar si fue retrasado comparando con el timeout del nodo
        boolean fueRetrasado = determinarRetraso(tarea, duracionMs);

        // Obtener etiqueta del nodo desde la política
        String etiquetaNodo = resolverEtiquetaNodo(tarea.getIdPolitica(), tarea.getIdNodo());

        // Archivar en historial (SAFlowNodeInstance: archiveDate, durationMs, stateId=2)
        TaskHistory historial = TaskHistory.builder()
                .idInstancia(tarea.getIdInstancia())
                .idPolitica(tarea.getIdPolitica())
                .idNodo(tarea.getIdNodo())
                .etiquetaNodo(etiquetaNodo)
                .idDepartamento(tarea.getIdDepartamentoAsignado())
                .idUsuario(tarea.getIdUsuarioAsignado())
                .nombreUsuario(tarea.getNombreUsuario())
                .duracionMs(duracionMs)
                .fueRetrasado(fueRetrasado)
                .fechaArchivo(fechaArchivo)
                .datos(formData)
                .build();
        historyRepo.save(historial);

        // Marcar semáforo VERDE (log de auditoría)
        tarea.setSemaforo(TrafficLight.VERDE);
        log.info("[WorkflowEngine] Tarea {} completada ({}ms, retrasado={})", tareaId, duracionMs, fueRetrasado);

        // Continuar flujo ANTES de eliminar: si derivarTramite falla,
        // la tarea original se conserva y puede reintentarse (MongoDB sin transacciones).
        Map<String, Object> datosAcumulados = mergeDatos(tarea.getDatos(), formData);
        derivarTramite(tarea.getIdInstancia(), datosAcumulados);

        // Eliminar tarea activa solo si derivarTramite tuvo éxito
        activeTaskRepo.deleteById(tareaId);

        return tarea;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EVALUADOR DE CONDICIONES SpEL  (adaptado de STransitionDefinition.getCondition)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Evalúa una expresión SpEL con los datos del formulario como contexto.
     * Ejemplo: "datos['decisionLegal'] == 'APROBADO'"
     *
     * Adaptado del patrón STransitionDefinition.hasCondition() + getCondition():
     *  - Si condition == null → transición por defecto (equivale a !hasCondition())
     *  - Si evalúa con error → loguea y retorna false (toma la transición por defecto)
     *
     * @param condition expresión SpEL (puede ser null)
     * @param data      variables del formulario
     * @return true si la condición se cumple o es null
     */
    public boolean evaluarCondicion(String condition, Map<String, Object> data) {
        String expr = normalizarCondicion(condition);
        if (expr.isBlank()) {
            return true; // sin condición = transición por defecto
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            Map<String, Object> safeData = data == null ? Map.of() : data;
            context.setRootObject(safeData);
            context.setVariable("datos", safeData);
            Expression expression = spelParser.parseExpression(expr);
            Boolean result = expression.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("[WorkflowEngine] Error evaluando condición SpEL '{}': {}. Tomando transición por defecto.",
                    condition, e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS PRIVADOS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Algoritmo getNextState adaptado de FlowNodeStateManagerImpl:
     *  1. Busca conexiones salientes con condición SpEL verdadera
     *  2. Si ninguna tiene SpEL, intenta coincidir el label de la conexión contra los valores del formulario
     *     (para gateways con etiquetas "si"/"no")
     *  3. Fallback: primera conexión sin condición
     *  4. Si el nodo destino es un gateway, lo atraviesa recursivamente (nunca crea tarea para un gateway)
     */
    private Node resolverNodoDestino(WorkflowDefinition politica, String nodoActualId,
                                     Map<String, Object> formData) {
        List<Connection> conexionesSalientes = politica.getConexiones().stream()
                .filter(c -> nodoActualId.equals(c.getSourceNodeId()))
                .toList();

        if (conexionesSalientes.isEmpty()) {
            throw new WorkflowException("El nodo '" + nodoActualId + "' no tiene conexiones salientes. "
                    + "Verifica si es un nodo END o si el diagrama está incompleto.");
        }

        // 1. Conexiones con condición SpEL verdadera
        Optional<Connection> conSpel = conexionesSalientes.stream()
                .filter(c -> c.getCondition() != null && !c.getCondition().isBlank())
                .filter(c -> evaluarCondicion(c.getCondition(), formData))
                .findFirst();

        if (conSpel.isPresent()) {
            return atravesarGateway(politica, conSpel.get().getTargetNodeId(), formData);
        }

        // 2. Sin SpEL: coincidir label de conexión contra valores del formulario
        //    Útil para gateways con etiquetas "si"/"no"
        Optional<Connection> porLabel = conexionesSalientes.stream()
                .filter(c -> c.getLabel() != null && !c.getLabel().isBlank())
                .filter(c -> labelCoincideConFormulario(c.getLabel(), formData))
                .findFirst();

        if (porLabel.isPresent()) {
            return atravesarGateway(politica, porLabel.get().getTargetNodeId(), formData);
        }

        // 3. Fallback: primera conexión sin condición ni label restrictivo
        Connection defaultConnection = conexionesSalientes.stream()
                .filter(c -> c.getCondition() == null || c.getCondition().isBlank())
                .findFirst()
                .orElseThrow(() -> new WorkflowException(
                        "No se encontró transición válida desde el nodo '" + nodoActualId
                                + "'. Ninguna condición SpEL resultó verdadera y no hay transición por defecto."));

        return atravesarGateway(politica, defaultConnection.getTargetNodeId(), formData);
    }

    /**
     * Si el nodo destino es un gateway, lo atraviesa automáticamente
     * (los gateways no son tareas: no tienen departamento ni formulario).
     */
    private Node atravesarGateway(WorkflowDefinition politica, String nodoId, Map<String, Object> formData) {
        Node nodo = resolverNodo(politica, nodoId);
        if (esGateway(nodo)) {
            log.debug("[WorkflowEngine] Atravesando gateway '{}' ({})", nodo.getLabel(), nodo.getType());
            return resolverNodoDestino(politica, nodoId, formData);
        }
        return nodo;
    }

    private boolean esGateway(Node nodo) {
        if (nodo.getType() == null) return false;
        String t = nodo.getType().toLowerCase();
        return t.contains("gateway");
    }

    private boolean esNodoFin(Node nodo) {
        if (nodo.getType() == null) return false;
        String t = nodo.getType().toLowerCase();
        // Acepta: "END", "EndEvent", "endEvent", "bpmn:EndEvent", etc.
        return t.equals("end") || t.contains("endevent") || t.contains("end_event");
    }

    private InstanceStatus determinarEstadoFinal(Node nodo) {
        String base = normalizarTexto(nodo.getLabel());
        if (base.isBlank()) {
            base = normalizarTexto(nodo.getId());
        }
        if (base.contains("cancel") || base.contains("rechaz") || base.contains("deneg")
                || base.contains("anul") || base.contains("no aprobado") || base.contains("noaprob")) {
            return InstanceStatus.CANCELADO;
        }
        return InstanceStatus.COMPLETADO;
    }

    private Map<String, Object> mergeDatos(Map<String, Object> base, Map<String, Object> extra) {
        Map<String, Object> merged = new HashMap<>();
        if (base != null) merged.putAll(base);
        if (extra != null) merged.putAll(extra);
        return merged;
    }

    private String extraerMotivoRechazo(Map<String, Object> datos) {
        if (datos == null || datos.isEmpty()) return null;
        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            String key = normalizarTexto(entry.getKey());
            if (key.contains("motivo") || key.contains("razon") || key.contains("causa")) {
                Object val = entry.getValue();
                String texto = val == null ? "" : val.toString().trim();
                if (!texto.isBlank()) return texto;
            }
        }
        return null;
    }

    /**
     * Comprueba si el label de una conexión coincide con algún valor del formulario.
     * Soporta patrones "si"/"no", "true"/"false", "1"/"0" y comparación directa.
     */
    private boolean labelCoincideConFormulario(String label, Map<String, Object> formData) {
        if (formData == null || formData.isEmpty()) return false;
        String lbl = normalizarTexto(label);
        return formData.values().stream().anyMatch(v -> {
            if (v == null) return false;
            String val = normalizarTexto(v.toString());
            // Mapeo booleano: "si" ↔ true/1, "no" ↔ false/0
            if (lbl.equals("si") || lbl.equals("yes") || lbl.equals("true") || lbl.equals("1")) {
                return val.equals("si") || val.equals("yes") || val.equals("true")
                        || val.equals("1") || val.equals("on");
            }
            if (lbl.equals("no") || lbl.equals("false") || lbl.equals("0")) {
                return val.equals("no") || val.equals("false") || val.equals("0")
                        || val.equals("off") || val.isEmpty();
            }
            return val.equals(lbl);
        });
    }

    private String normalizarCondicion(String condition) {
        if (condition == null) return "";
        String expr = condition.trim();
        if ((expr.startsWith("${") || expr.startsWith("#{")) && expr.endsWith("}")) {
            expr = expr.substring(2, expr.length() - 1).trim();
        }
        return expr;
    }

    private String normalizarTexto(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized.toLowerCase().trim();
    }

    private Node resolverNodo(WorkflowDefinition politica, String nodoId) {
        if (nodoId == null) {
            throw new WorkflowException("El ID de nodo es nulo en la política: " + politica.getId());
        }
        return politica.getNodos().stream()
                .filter(n -> nodoId.equals(n.getId()))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(nodoId, politica.getId()));
    }

    private boolean determinarRetraso(ActiveTask tarea, long duracionMs) {
        if (tarea.getIdPolitica() == null || tarea.getIdNodo() == null) return false;
        return policyRepo.findById(tarea.getIdPolitica())
                .flatMap(pol -> pol.getNodos().stream()
                        .filter(n -> tarea.getIdNodo().equals(n.getId()))
                        .findFirst())
                .map(nodo -> {
                    long timeoutMs = (long) (nodo.getTimeoutHours() * 3_600_000L);
                    return timeoutMs > 0 && duracionMs > timeoutMs;
                })
                .orElse(false);
    }

    private String resolverEtiquetaNodo(String idPolitica, String idNodo) {
        if (idPolitica == null || idNodo == null) return idNodo;
        return policyRepo.findById(idPolitica)
                .flatMap(pol -> pol.getNodos().stream()
                        .filter(n -> idNodo.equals(n.getId()))
                        .findFirst())
                .map(Node::getLabel)
                .orElse(idNodo);
    }

    private void enviarNotificacion(String idUsuario, String titulo, String mensaje,
                                    String tipo, String idInstancia, String idTarea) {
        Notification notif = Notification.builder()
                .idUsuario(idUsuario)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(tipo)
                .idInstancia(idInstancia)
                .idTarea(idTarea)
                .build();
        notificationRepo.save(notif);
    }

    private void notificarCliente(ProcessInstance instancia, String tipo) {
        if (instancia.getIdCliente() == null || instancia.getIdCliente().isBlank()) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", tipo);
        payload.put("instanciaId", instancia.getId());
        payload.put("estado", instancia.getEstado());
        payload.put("departamento", instancia.getIdDepartamentoActual());
        payload.put("nodoActualId", instancia.getNodoActualId());
        payload.put("motivoRechazo", instancia.getMotivoRechazo());
        payload.put("timestamp", Instant.now().toString());
        messaging.convertAndSend("/topic/notificaciones/cliente/" + instancia.getIdCliente(), (Object) payload);
    }

    // Referencia a constantes de tipo de nodo ya definidas en com.workflow.common.Constants
    private static class Constants {
        static final String NODE_TYPE_END = "END";
    }
}
