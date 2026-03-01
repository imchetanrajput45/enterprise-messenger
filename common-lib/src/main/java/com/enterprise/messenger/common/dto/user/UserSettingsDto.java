package com.enterprise.messenger.common.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {

    private boolean readReceipts;
    private boolean typingIndicator;
    private boolean onlineStatus;
    private String notificationSound;
    private String theme;
}
