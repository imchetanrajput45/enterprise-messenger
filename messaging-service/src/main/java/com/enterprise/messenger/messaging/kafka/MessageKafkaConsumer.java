package com.enterprise.messenger.messaging.kafka;

import com.enterprise.messenger.common.config.KafkaTopics;
import com.enterprise.messenger.common.dto.messaging.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens for message events and broadcasts via WebSocket.
 * This enables horizontal scaling — multiple messaging-service instances share Kafka consumers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageKafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaTopics.MESSAGE_SENT, groupId = "messaging-service-group")
    public void handleMessageSent(ChatMessageEvent event) {
        log.debug("Consumed MESSAGE_SENT: {} in conversation {}", event.getMessageId(), event.getConversationId());
        // Broadcast to all subscribers of the conversation via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + event.getConversationId(),
                event
        );
    }

    @KafkaListener(topics = KafkaTopics.MESSAGE_DELIVERED, groupId = "messaging-service-group")
    public void handleMessageDelivered(ChatMessageEvent event) {
        log.debug("Consumed MESSAGE_DELIVERED: {}", event.getMessageId());
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + event.getConversationId() + "/status",
                event
        );
    }

    @KafkaListener(topics = KafkaTopics.MESSAGE_READ, groupId = "messaging-service-group")
    public void handleMessageRead(ChatMessageEvent event) {
        log.debug("Consumed MESSAGE_READ: {}", event.getMessageId());
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + event.getConversationId() + "/status",
                event
        );
    }
}
