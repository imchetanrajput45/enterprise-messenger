package com.enterprise.messenger.common.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private String userId;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    private String avatarUrl;

    @Size(max = 255, message = "Status text must not exceed 255 characters")
    private String statusText;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private String timezone;
    private String locale;
}
