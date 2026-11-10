package com.example.userservice.security

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * This service is an OAuth2 resource server: it never sees a password. A caller sends the access token the
 * auth server issued, Spring Security checks its signature, issuer and expiry, and turns its claims into authorities:
 * scope users.read -> SCOPE_users.read, roles ["ADMIN"] -> ROLE_ADMIN.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity // switches on @PreAuthorize in the controller
class SecurityConfig {

    @Bean
    fun apiSecurity(
        http: HttpSecurity,
        @Qualifier("handlerExceptionResolver") errorResolver: HandlerExceptionResolver,
    ): SecurityFilterChain {
        // Spring Security's Kotlin DSL: http { ... } instead of the lambda chain of the Java twin
        http {
            // stateless API with a bearer token in a header: no cookie and no session, so nothing for CSRF to abuse
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            // coarse rules by URL: the TOKEN's scope. Fine rules (the user's role) are @PreAuthorize in the controller
            authorizeHttpRequests {
                authorize("/actuator/health/**", permitAll)
                authorize(HttpMethod.GET, "/api/**", hasAuthority("SCOPE_users.read"))
                authorize("/api/**", hasAuthority("SCOPE_users.write"))
                authorize(anyRequest, denyAll)
            }
            oauth2ResourceServer {
                jwt { jwtAuthenticationConverter = jwtAuthenticationConverter() }
                // hand 401 / 403 to the @RestControllerAdvice, so they look like every other error
                authenticationEntryPoint = AuthenticationEntryPoint { request, response, ex ->
                    errorResolver.resolveException(request, response, null, ex)
                }
                accessDeniedHandler = AccessDeniedHandler { request, response, ex ->
                    errorResolver.resolveException(request, response, null, ex)
                }
            }
        }
        return http.build()
    }

    /** Authorities = the "scope" claim as SCOPE_x (Spring's default) plus the "roles" claim as ROLE_x (our addition). */
    private fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val scopes = JwtGrantedAuthoritiesConverter()
        val roles = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("roles")
            setAuthorityPrefix("ROLE_")
        }
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter { jwt -> scopes.convert(jwt).orEmpty() + roles.convert(jwt).orEmpty() }
        }
    }
}
