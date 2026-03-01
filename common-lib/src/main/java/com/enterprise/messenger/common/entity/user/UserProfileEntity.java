package com.enterprise.messenger.common.entity.user;

import com.enterprise.messenger.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * User profile entity — stores display information, avatar, and status.
 */
@Entity
@Table(name = "user_profiles", schema = "user_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileEntity extends BaseEntity {

    @Column(name = "auth_user_id", nullable = false, unique = true)
    private java.util.UUID authUserId;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "status_text", length = 255)
    @Builder.Default
    private String statusText = "Hey there! I am using Enterprise Messenger";

    @Column(length = 500)
    private String bio;

    @Column(length = 50)
    private String timezone;

    @Column(length = 10)
    @Builder.Default
    private String locale = "en";
}
