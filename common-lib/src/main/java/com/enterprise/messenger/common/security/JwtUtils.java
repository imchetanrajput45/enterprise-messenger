package com.enterprise.messenger.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

/**
 * Shared JWT utility for extracting claims from tokens.
 * Used by the API Gateway and individual microservices
 * to read user identity from validated tokens.
 */
public final class JwtUtils {

    private JwtUtils() {
        // Utility class — no instantiation
    }

    /**
     * Parse all claims from a JWT token string.
     */
    public static Claims extractAllClaims(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the subject (username) from the token.
     */
    public static String extractUsername(String token, String secret) {
        return extractAllClaims(token, secret).getSubject();
    }

    /**
     * Extract the user ID stored in the "userId" claim.
     */
    public static UUID extractUserId(String token, String secret) {
        return UUID.fromString(extractAllClaims(token, secret).get("userId", String.class));
    }

    /**
     * Extract roles from the "roles" claim.
     */
    @SuppressWarnings("unchecked")
    public static List<String> extractRoles(String token, String secret) {
        return extractAllClaims(token, secret).get("roles", List.class);
    }

    /**
     * Check if the token has expired.
     */
    public static boolean isTokenExpired(String token, String secret) {
        return extractAllClaims(token, secret)
                .getExpiration()
                .toInstant()
                .isBefore(java.time.Instant.now());
    }
}
