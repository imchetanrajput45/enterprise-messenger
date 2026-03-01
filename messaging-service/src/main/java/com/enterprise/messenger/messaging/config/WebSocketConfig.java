package com.enterprise.messenger.messaging.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP configuration.
 * <p>
 * Clients connect to /ws endpoint.
 * Subscribe to /topic/conversations/{id} for group broadcasts.
 * Subscribe to /queue/messages for personal message delivery.
 * Send messages to /app/chat.send
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic — broadcast to all subscribers (group conversations)
        // /queue — point-to-point delivery (personal notifications)
        config.enableSimpleBroker("/topic", "/queue");
        // Client messages prefixed with /app are routed to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
        // User-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback for older browsers
    }
}
