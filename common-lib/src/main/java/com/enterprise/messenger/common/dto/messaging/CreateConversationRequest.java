package com.enterprise.messenger.common.dto.messaging;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

    /**
     * DIRECT or GROUP. If DIRECT, memberIds must contain exactly 1 other user.
     */
    private String type;

    @Size(max = 100, message = "Conversation name must not exceed 100 characters")
    private String name;

    private String avatarUrl;

    @NotEmpty(message = "At least one member is required")
    private List<UUID> memberIds;
}
