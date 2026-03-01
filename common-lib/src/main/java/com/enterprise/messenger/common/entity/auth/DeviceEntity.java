package com.enterprise.messenger.common.entity.auth;

import com.enterprise.messenger.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Device registry entity for multi-device support.
 */
@Entity
@Table(name = "devices", schema = "auth_schema",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 50)
    private DeviceType deviceType;

    @Column(name = "push_token", length = 512)
    private String pushToken;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Column(name = "is_trusted", nullable = false)
    @Builder.Default
    private boolean trusted = false;

    public enum DeviceType {
        MOBILE, DESKTOP, WEB
    }
}
