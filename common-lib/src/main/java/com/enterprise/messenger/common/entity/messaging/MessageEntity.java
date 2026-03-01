package com.enterprise.messenger.common.entity.messaging;

import com.enterprise.messenger.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Message entity — individual chat message within a conversation.
 */
@Entity
@Table(name = "messages", schema = "messaging_schema",
        indexes = {
                @Index(name = "idx_messages_conversation_id", columnList = "conversation_id"),
                @Index(name = "idx_messages_sender_id", columnList = "sender_id"),
                @Index(name = "idx_messages_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_url", length = 512)
    private String mediaUrl;

    @Column(name = "media_type", length = 50)
    private String mediaType;

    @Column(name = "reply_to_id")
    private UUID replyToId;

    @Column(nullable = false)
    @Builder.Default
    private boolean edited = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    public enum MessageType {
        TEXT, IMAGE, VIDEO, AUDIO, FILE, SYSTEM
    }
}
