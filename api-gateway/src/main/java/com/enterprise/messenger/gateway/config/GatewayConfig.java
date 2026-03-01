package com.enterprise.messenger.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route definitions for the API gateway.
 * Routes are resolved via Eureka service discovery (lb:// prefix).
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ─── Auth Service ──────────────────────────
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://AUTH-SERVICE"))

                // ─── User Service ──────────────────────────
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("lb://USER-SERVICE"))

                // ─── Messaging Service (Phase 2) ───────────
                .route("messaging-ws", r -> r
                        .path("/ws/**")
                        .uri("lb://MESSAGING-SERVICE"))

                .route("messaging-service", r -> r
                        .path("/api/messages/**", "/api/conversations/**")
                        .uri("lb://MESSAGING-SERVICE"))

                // ─── Presence Service (Phase 4) ────────────
                .route("presence-service", r -> r
                        .path("/api/presence/**")
                        .uri("lb://PRESENCE-SERVICE"))

                // ─── Media Service (Phase 5) ───────────────
                .route("media-service", r -> r
                        .path("/api/media/**")
                        .uri("lb://MEDIA-SERVICE"))

                .build();
    }
}
