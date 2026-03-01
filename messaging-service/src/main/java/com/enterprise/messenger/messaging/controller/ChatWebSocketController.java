package com.enterprise.messenger.messaging.controller;

import com.enterprise.messenger.common.dto.messaging.SendMessageRequest;
import com.enterprise.messenger.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket STOMP controller for real-time chat messages.
 * <p>
 * Clients send messages to /app/chat.send
 * Messages are persisted, published to Kafka, and broadcast to /topic/conversations/{id}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming WebSocket messages.
     * Client sends to: /app/chat.send
     * X-User-Id header is forwarded by the gateway/client.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request,
                            @Header("X-User-Id") String userId) {
        log.debug("WebSocket message from user {} to conversation {}", userId, request.getConversationId());
        messageService.sendMessage(UUID.fromString(userId), request);
        // Response is broadcast via Kafka consumer → WebSocket /topic/conversations/{id}
    }

    /**
     * Handle typing indicator.
     * Client sends to: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingPayload payload,
                       @Header("X-User-Id") String userId) {
        log.debug("Typing indication from user {} in conversation {}", userId, payload.conversationId());
        // Broadcast typing event to conversation participants
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + payload.conversationId() + "/typing",
                new TypingEvent(payload.conversationId(), UUID.fromString(userId))
        );
    }

    public record TypingPayload(UUID conversationId) {}
    public record TypingEvent(UUID conversationId, UUID userId) {}
}
