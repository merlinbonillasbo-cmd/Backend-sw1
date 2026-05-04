package com.workflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // El broker simple publica mensajes a clientes suscritos en /topic
        registry.enableSimpleBroker("/topic");
        // Prefijo para mensajes que van al servidor (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de conexión WebSocket con fallback SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "http://localhost:4200",
                    "http://18.222.251.205",
                    "http://18.222.251.205:80",
                    "http://18.222.251.205:4200",
                    "http://18.222.251.205:8000",
                    "http://18.224.95.208",
                    "http://18.224.95.208:80",
                    "http://18.224.95.208:4200",
                    "http://18.224.95.208:8000"
                )
                .withSockJS();
    }
}

