package com.enterprise.messenger.common.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {

    private UUID id;
    private String type;
    private String name;
    private String avatarUrl;
    private UUID createdBy;
    private Instant createdAt;

    /** Last message preview */
    private MessageDto lastMessage;

    /** Unread message count for the requesting user */
    private long unreadCount;

    /** Member summaries */
    private List<ConversationMemberDto> members;
}
