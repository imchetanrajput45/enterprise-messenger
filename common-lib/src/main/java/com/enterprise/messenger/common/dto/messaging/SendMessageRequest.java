package com.enterprise.messenger.common.dto.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Conversation ID is required")
    private UUID conversationId;

    @NotBlank(message = "Content is required")
    private String content;

    private String messageType; // TEXT, IMAGE, VIDEO, AUDIO, FILE — defaults to TEXT

    private String mediaUrl;

    private String mediaType;

    /** Reply to a specific message */
    private UUID replyToId;
}
