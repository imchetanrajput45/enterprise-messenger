package com.enterprise.messenger.common.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private String messageType;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private UUID replyToId;
    private boolean edited;
    private boolean deleted;
    private String localStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
