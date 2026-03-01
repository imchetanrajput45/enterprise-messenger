package com.enterprise.messenger.common.entity.user;

import com.enterprise.messenger.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Contact (address book) entity.
 */
@Entity
@Table(name = "contacts", schema = "user_schema",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "contact_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactEntity extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "contact_id", nullable = false)
    private UUID contactId;

    @Column(length = 100)
    private String nickname;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private boolean blocked = false;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private boolean favorite = false;
}
