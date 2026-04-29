package com.workflow.modules.pizarra;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Maneja la presencia de diseñadores en la pizarra colaborativa.
 * Escucha mensajes en /app/pizarra/{flujoId}/presencia y emite
 * al topic /topic/pizarra/{flujoId}/presencia la lista actualizada.
 */
@Controller
public class PizarraWebSocketController {

    private final SimpMessagingTemplate messaging;

    // flujoId → (userId → nombreCompleto)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> presencia =
            new ConcurrentHashMap<>();

    public PizarraWebSocketController(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @MessageMapping("/pizarra/{flujoId}/presencia")
    public void manejarPresencia(
            @DestinationVariable String flujoId,
            @Payload Map<String, String> payload) {

        String tipo   = payload.getOrDefault("tipo", "");
        String userId = payload.getOrDefault("userId", "anon");
        String nombre = payload.getOrDefault("nombre", "Anónimo");

        presencia.computeIfAbsent(flujoId, k -> new ConcurrentHashMap<>());

        if ("USUARIO_CONECTADO".equals(tipo)) {
            presencia.get(flujoId).put(userId, nombre);
        } else if ("USUARIO_DESCONECTADO".equals(tipo)) {
            presencia.get(flujoId).remove(userId);
            if (presencia.get(flujoId).isEmpty()) {
                presencia.remove(flujoId);
            }
        }

        List<Map<String, String>> usuarios = presencia
                .getOrDefault(flujoId, new ConcurrentHashMap<>())
                .entrySet().stream()
                .map(e -> Map.of("userId", e.getKey(), "nombre", e.getValue()))
                .collect(Collectors.toList());

        Map<String, Object> presenciaPayload = new java.util.HashMap<>();
        presenciaPayload.put("tipo",     "PRESENCIA_ACTUALIZADA");
        presenciaPayload.put("usuarios", usuarios);
        messaging.convertAndSend("/topic/pizarra/" + flujoId + "/presencia", (Object) presenciaPayload);
    }
}
