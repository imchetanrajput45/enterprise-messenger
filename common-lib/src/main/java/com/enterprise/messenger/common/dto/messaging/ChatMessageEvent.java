package com.enterprise.messenger.common.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket/Kafka event payload for real-time message broadcasting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent {

    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;
    private String senderUsername;
    private String messageType;
    private String content;
    private String mediaUrl;
    private UUID replyToId;
    private Instant timestamp;

    /** Event type: SENT, DELIVERED, READ, TYPING, EDITED, DELETED */
    private String eventType;
}
