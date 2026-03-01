package com.enterprise.messenger.messaging.kafka;

import com.enterprise.messenger.common.config.KafkaTopics;
import com.enterprise.messenger.common.dto.messaging.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for messaging events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageKafkaProducer {

    private final KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;

    public void publishMessageSent(ChatMessageEvent event) {
        log.debug("Publishing MESSAGE_SENT event for message: {}", event.getMessageId());
        kafkaTemplate.send(KafkaTopics.MESSAGE_SENT, event.getConversationId().toString(), event);
    }

    public void publishMessageDelivered(ChatMessageEvent event) {
        log.debug("Publishing MESSAGE_DELIVERED event for message: {}", event.getMessageId());
        kafkaTemplate.send(KafkaTopics.MESSAGE_DELIVERED, event.getConversationId().toString(), event);
    }

    public void publishMessageRead(ChatMessageEvent event) {
        log.debug("Publishing MESSAGE_READ event for message: {}", event.getMessageId());
        kafkaTemplate.send(KafkaTopics.MESSAGE_READ, event.getConversationId().toString(), event);
    }
}
