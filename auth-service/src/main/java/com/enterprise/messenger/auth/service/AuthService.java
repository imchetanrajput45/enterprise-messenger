package com.enterprise.messenger.auth.service;

import com.enterprise.messenger.common.dto.auth.AuthResponse;
import com.enterprise.messenger.common.dto.auth.LoginRequest;
import com.enterprise.messenger.common.dto.auth.RefreshTokenRequest;
import com.enterprise.messenger.common.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);
}
