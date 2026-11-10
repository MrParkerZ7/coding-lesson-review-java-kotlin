package com.example.userservice.web;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Replaces the real JwtDecoder (which would call the auth server) with a table of fake tokens.
 * The request still goes through the whole filter chain, so SecurityConfig's claim-to-authority conversion is tested too.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestJwtDecoderConfig {

    static final String ADMIN = "admin";
    static final String ADMIN_READ_ONLY = "admin-read-only";
    static final String ALICE = "alice";
    static final String GHOST = "ghost";
    static final String NO_SUBJECT = "no-subject";

    @Bean
    JwtDecoder jwtDecoder() {
        Map<String, Jwt> tokens = Map.of(
                ADMIN, jwt("admin", List.of("openid", "users.read", "users.write"), List.of("ADMIN")),
                ADMIN_READ_ONLY, jwt("admin", List.of("openid", "users.read"), List.of("ADMIN")),
                ALICE, jwt("alice", List.of("openid", "users.read", "users.write"), List.of("USER")),
                GHOST, jwt("ghost", List.of("openid", "users.read", "users.write"), List.of("USER")),
                NO_SUBJECT, jwt(null, List.of("openid", "users.read", "users.write"), List.of("USER")));
        return token -> {
            Jwt jwt = tokens.get(token);
            if (jwt == null) {
                throw new BadJwtException("not a token this test knows");
            }
            return jwt;
        };
    }

    private static Jwt jwt(@Nullable String subject, List<String> scopes, List<String> roles) {
        Jwt.Builder builder = Jwt.withTokenValue("irrelevant")
                .header("alg", "none")
                .claim("scope", scopes)
                .claim("roles", roles)
                .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .expiresAt(Instant.parse("2126-01-01T00:00:00Z"));
        if (subject != null) {
            builder.subject(subject);
        }
        return builder.build();
    }
}
