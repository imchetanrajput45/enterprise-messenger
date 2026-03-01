package com.enterprise.messenger.user.service.impl;

import com.enterprise.messenger.common.dto.user.UserProfileDto;
import com.enterprise.messenger.common.entity.user.UserProfileEntity;
import com.enterprise.messenger.common.exception.BadRequestException;
import com.enterprise.messenger.common.exception.ResourceNotFoundException;
import com.enterprise.messenger.user.repository.UserProfileRepository;
import com.enterprise.messenger.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        UserProfileEntity profile = profileRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));
        return toDto(profile);
    }

    @Override
    @Transactional
    public UserProfileDto createProfile(UUID userId, UserProfileDto dto) {
        if (profileRepository.existsByAuthUserId(userId)) {
            throw new BadRequestException("Profile already exists for user: " + userId);
        }

        UserProfileEntity profile = UserProfileEntity.builder()
                .authUserId(userId)
                .displayName(dto.getDisplayName())
                .avatarUrl(dto.getAvatarUrl())
                .statusText(dto.getStatusText())
                .bio(dto.getBio())
                .timezone(dto.getTimezone())
                .locale(dto.getLocale() != null ? dto.getLocale() : "en")
                .build();

        profile = profileRepository.save(profile);
        log.info("Profile created for user: {}", userId);
        return toDto(profile);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UUID userId, UserProfileDto dto) {
        UserProfileEntity profile = profileRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        if (dto.getDisplayName() != null) profile.setDisplayName(dto.getDisplayName());
        if (dto.getAvatarUrl() != null) profile.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getStatusText() != null) profile.setStatusText(dto.getStatusText());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getTimezone() != null) profile.setTimezone(dto.getTimezone());
        if (dto.getLocale() != null) profile.setLocale(dto.getLocale());

        profile = profileRepository.save(profile);
        log.info("Profile updated for user: {}", userId);
        return toDto(profile);
    }

    private UserProfileDto toDto(UserProfileEntity entity) {
        return UserProfileDto.builder()
                .userId(entity.getAuthUserId().toString())
                .displayName(entity.getDisplayName())
                .avatarUrl(entity.getAvatarUrl())
                .statusText(entity.getStatusText())
                .bio(entity.getBio())
                .timezone(entity.getTimezone())
                .locale(entity.getLocale())
                .build();
    }
}
