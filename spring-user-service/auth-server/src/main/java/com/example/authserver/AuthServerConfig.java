package com.example.authserver;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration(proxyBeanMethods = false)
class AuthServerConfig {

    /**
     * Who can log in. DEMO ONLY: {noop} stores the password as plain text, a real server stores a bcrypt hash.
     * Their usernames match the profiles the user services seed, so GET /api/users/me works out of the box.
     */
    @Bean
    UserDetailsService users() {
        UserDetails alice = User.withUsername("alice").password("{noop}password").roles("USER").build();
        UserDetails bob = User.withUsername("bob").password("{noop}password").roles("USER").build();
        UserDetails admin = User.withUsername("admin").password("{noop}password").roles("ADMIN").build();
        return new InMemoryUserDetailsManager(alice, bob, admin);
    }

    /**
     * Adds a "roles" claim to every access token: ["USER"] or ["ADMIN"].
     * The resource servers turn it into ROLE_USER / ROLE_ADMIN. "scope" is added by the server itself.
     */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> rolesClaim() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }
            List<String> roles = context.getPrincipal().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .sorted()
                    .toList();
            context.getClaims().claim("roles", roles);
        };
    }
}
