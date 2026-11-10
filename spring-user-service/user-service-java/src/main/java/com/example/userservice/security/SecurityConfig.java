package com.example.userservice.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * This service is an OAuth2 resource server: it never sees a password. A caller sends the access token the
 * auth server issued, Spring Security checks its signature, issuer and expiry, and turns its claims into authorities:
 * scope users.read -> SCOPE_users.read, roles ["ADMIN"] -> ROLE_ADMIN.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity // switches on @PreAuthorize in the controller
class SecurityConfig {

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver errorResolver) throws Exception {
        http
                // stateless API with a bearer token in a header: no cookie and no session, so nothing for CSRF to abuse
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // coarse rules by URL: the TOKEN's scope. Fine rules (the user's role) are @PreAuthorize in the controller
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("SCOPE_users.read")
                        .requestMatchers("/api/**").hasAuthority("SCOPE_users.write")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        // hand 401 / 403 to the @RestControllerAdvice, so they look like every other error
                        .authenticationEntryPoint((request, response, ex) -> errorResolver.resolveException(request, response, null, ex))
                        .accessDeniedHandler((request, response, ex) -> errorResolver.resolveException(request, response, null, ex)));
        return http.build();
    }

    /** Authorities = the "scope" claim as SCOPE_x (Spring's default) plus the "roles" claim as ROLE_x (our addition). */
    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            addAll(authorities, scopes.convert(jwt));
            addAll(authorities, roles.convert(jwt));
            return authorities;
        });
        return converter;
    }

    private static void addAll(List<GrantedAuthority> target, Collection<GrantedAuthority> source) {
        target.addAll(source);
    }
}
