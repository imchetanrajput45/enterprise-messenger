package com.enterprise.messenger.user.controller;

import com.enterprise.messenger.common.dto.ApiResponse;
import com.enterprise.messenger.common.dto.user.UserProfileDto;
import com.enterprise.messenger.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        UserProfileDto profile = userProfileService.getProfile(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@PathVariable UUID userId) {
        UserProfileDto profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserProfileDto>> createProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserProfileDto dto) {
        UserProfileDto profile = userProfileService.createProfile(UUID.fromString(userId), dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created", profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserProfileDto dto) {
        UserProfileDto profile = userProfileService.updateProfile(UUID.fromString(userId), dto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
    }
}
