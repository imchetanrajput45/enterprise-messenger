package com.enterprise.messenger.auth.service.impl;

import com.enterprise.messenger.common.dto.auth.AuthResponse;
import com.enterprise.messenger.common.dto.auth.LoginRequest;
import com.enterprise.messenger.common.dto.auth.RefreshTokenRequest;
import com.enterprise.messenger.common.dto.auth.RegisterRequest;
import com.enterprise.messenger.common.entity.auth.RefreshTokenEntity;
import com.enterprise.messenger.common.entity.auth.RoleEntity;
import com.enterprise.messenger.common.entity.auth.UserEntity;
import com.enterprise.messenger.auth.repository.RefreshTokenRepository;
import com.enterprise.messenger.auth.repository.RoleRepository;
import com.enterprise.messenger.auth.repository.UserRepository;
import com.enterprise.messenger.auth.security.JwtTokenProvider;
import com.enterprise.messenger.auth.service.AuthService;
import com.enterprise.messenger.common.exception.BadRequestException;
import com.enterprise.messenger.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already registered");
        }

        RoleEntity defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", DEFAULT_ROLE));

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(defaultRole))
                .build();

        user = userRepository.save(user);
        log.info("User registered: {} ({})", user.getUsername(), user.getId());

        List<String> roles = List.of(DEFAULT_ROLE);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = createRefreshToken(user, null);

        return buildAuthResponse(user, accessToken, refreshToken, roles);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (user.isAccountLocked()) {
            throw new BadRequestException("Account is locked. Try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                userRepository.save(user);
            }

            List<String> roles = user.getRoles().stream()
                    .map(RoleEntity::getName)
                    .toList();

            String accessToken = jwtTokenProvider.generateAccessToken(authentication, user.getId());
            String refreshToken = createRefreshToken(user, request.getDeviceId());

            log.info("User logged in: {}", user.getUsername());
            return buildAuthResponse(user, accessToken, refreshToken, roles);

        } catch (Exception ex) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setAccountStatus(UserEntity.AccountStatus.LOCKED);
                user.setLockedUntil(Instant.now().plusMillis(LOCK_DURATION_MS));
                log.warn("Account locked due to {} failed attempts: {}", MAX_FAILED_ATTEMPTS, user.getUsername());
            }
            userRepository.save(user);
            throw new BadRequestException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshTokenEntity.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(refreshTokenEntity.getUser().getId());
            throw new BadRequestException("Refresh token has been revoked. All sessions invalidated.");
        }

        if (refreshTokenEntity.isExpired()) {
            refreshTokenEntity.setRevoked(true);
            refreshTokenRepository.save(refreshTokenEntity);
            throw new BadRequestException("Refresh token has expired. Please login again.");
        }

        UserEntity user = refreshTokenEntity.getUser();
        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .toList();

        refreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(refreshTokenEntity);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        String newRefreshToken = createRefreshToken(user, refreshTokenEntity.getDeviceId());

        log.info("Token refreshed for user: {}", user.getUsername());
        return buildAuthResponse(user, newAccessToken, newRefreshToken, roles);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("User logged out, refresh token revoked");
                });
    }

    private String createRefreshToken(UserEntity user, String deviceId) {
        if (deviceId != null) {
            refreshTokenRepository.revokeByUserIdAndDeviceId(user.getId(), deviceId);
        }

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private AuthResponse buildAuthResponse(UserEntity user, String accessToken,
                                            String refreshToken, List<String> roles) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .userId(user.getId().toString())
                .username(user.getUsername())
                .roles(roles)
                .build();
    }
}
