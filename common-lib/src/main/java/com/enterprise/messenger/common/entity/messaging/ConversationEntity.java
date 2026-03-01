package com.enterprise.messenger.common.entity.messaging;

import com.enterprise.messenger.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversation entity — represents a 1:1 (DIRECT) or group (GROUP) chat.
 */
@Entity
@Table(name = "conversations", schema = "messaging_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationType type;

    @Column(length = 100)
    private String name;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "created_by", nullable = false)
    private java.util.UUID createdBy;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConversationMemberEntity> members = new ArrayList<>();

    public enum ConversationType {
        DIRECT, GROUP
    }
}
