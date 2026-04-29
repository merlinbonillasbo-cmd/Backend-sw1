package com.workflow.modules.officer.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class OfficerWebSocketController {

    private final SimpMessagingTemplate messaging;

    // departamentoCodigo → { idUsuario → nombreUsuario }
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> presenciasPorDepartamento = new ConcurrentHashMap<>();

    public OfficerWebSocketController(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @MessageMapping("/officer/{deptCodigo}/presencia")
    public void registrarPresencia(
            @DestinationVariable String deptCodigo,
            Map<String, Object> payload) {

        String tipo = (String) payload.get("tipo");
        String idUsuario = (String) payload.get("idUsuario");
        String nombreUsuario = (String) payload.get("nombreUsuario");

        presenciasPorDepartamento.putIfAbsent(deptCodigo, new ConcurrentHashMap<>());
        ConcurrentHashMap<String, String> usuarios = presenciasPorDepartamento.get(deptCodigo);

        if ("CONECTADO".equals(tipo)) {
            usuarios.put(idUsuario, nombreUsuario);
        } else if ("DESCONECTADO".equals(tipo)) {
            usuarios.remove(idUsuario);
        }

        // Broadcast a todos los conectados del departamento
        Map<String, Object> respuesta = new java.util.HashMap<>();
        respuesta.put("tipo", "PRESENCIA_ACTUALIZADA");
        respuesta.put("usuarios", usuarios.entrySet().stream()
            .map(e -> Map.of("idUsuario", e.getKey(), "nombreUsuario", e.getValue()))
            .toList());
        respuesta.put("timestamp", Instant.now().toString());

        messaging.convertAndSend("/topic/presencia/" + deptCodigo, (Object) respuesta);
    }

    /**
     * Enviar notificación a todos del departamento (llamado desde otros servicios)
     */
    public void notificarDepartamento(String deptCodigo, String tipoEvento, Map<String, Object> datos) {
        Map<String, Object> notificacion = new java.util.HashMap<>();
        notificacion.put("tipo", tipoEvento);
        notificacion.putAll(datos);
        notificacion.put("timestamp", Instant.now().toString());

        messaging.convertAndSend("/topic/notificaciones/" + deptCodigo, (Object) notificacion);
    }
}
