package com.enterprise.messenger.user.service;

import com.enterprise.messenger.common.dto.user.UserProfileDto;

import java.util.UUID;

public interface UserProfileService {

    UserProfileDto getProfile(UUID userId);

    UserProfileDto createProfile(UUID userId, UserProfileDto dto);

    UserProfileDto updateProfile(UUID userId, UserProfileDto dto);
}
