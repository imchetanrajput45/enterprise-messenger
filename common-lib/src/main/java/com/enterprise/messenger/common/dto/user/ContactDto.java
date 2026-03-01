package com.enterprise.messenger.common.dto.user;

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
public class ContactDto {

    private UUID id;

    @NotNull(message = "Contact ID is required")
    private UUID contactId;

    private String nickname;
    private boolean blocked;
    private boolean favorite;

    private String displayName;
    private String avatarUrl;
}
