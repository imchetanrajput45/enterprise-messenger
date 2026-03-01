package com.enterprise.messenger.common.entity.auth;

import com.enterprise.messenger.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity for RBAC.
 */
@Entity
@Table(name = "roles", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;
}
