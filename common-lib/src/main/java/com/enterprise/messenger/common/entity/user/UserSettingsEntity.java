package com.enterprise.messenger.common.entity.user;

import com.enterprise.messenger.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * User settings entity — privacy and notification preferences.
 */
@Entity
@Table(name = "user_settings", schema = "user_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingsEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "read_receipts", nullable = false)
    @Builder.Default
    private boolean readReceipts = true;

    @Column(name = "typing_indicator", nullable = false)
    @Builder.Default
    private boolean typingIndicator = true;

    @Column(name = "online_status", nullable = false)
    @Builder.Default
    private boolean onlineStatus = true;

    @Column(name = "notification_sound", length = 100)
    @Builder.Default
    private String notificationSound = "default";

    @Column(length = 20)
    @Builder.Default
    private String theme = "SYSTEM";
}
