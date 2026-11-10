package com.example.userservice.web

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.time.Instant

/**
 * Replaces the real JwtDecoder (which would call the auth server) with a table of fake tokens.
 * The request still goes through the whole filter chain, so SecurityConfig's claim-to-authority conversion is tested too.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestJwtDecoderConfig {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val all = listOf("openid", "users.read", "users.write")
        val tokens = mapOf(
            ADMIN to jwt("admin", all, listOf("ADMIN")),
            ADMIN_READ_ONLY to jwt("admin", listOf("openid", "users.read"), listOf("ADMIN")),
            ALICE to jwt("alice", all, listOf("USER")),
            GHOST to jwt("ghost", all, listOf("USER")),
            NO_SUBJECT to jwt(null, all, listOf("USER")),
        )
        return JwtDecoder { token -> tokens[token] ?: throw BadJwtException("not a token this test knows") }
    }

    private fun jwt(subject: String?, scopes: List<String>, roles: List<String>): Jwt {
        val builder = Jwt.withTokenValue("irrelevant")
            .header("alg", "none")
            .claim("scope", scopes)
            .claim("roles", roles)
            .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .expiresAt(Instant.parse("2126-01-01T00:00:00Z"))
        if (subject != null) builder.subject(subject)
        return builder.build()
    }

    companion object {
        const val ADMIN = "admin"
        const val ADMIN_READ_ONLY = "admin-read-only"
        const val ALICE = "alice"
        const val GHOST = "ghost"
        const val NO_SUBJECT = "no-subject"
    }
}
