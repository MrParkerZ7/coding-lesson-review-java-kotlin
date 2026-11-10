package com.example.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The one OAuth2 authorization server both user services trust.
 * Everything about clients and the signing key comes from application.yml and Boot auto-configuration;
 * the only code is AuthServerConfig (demo users + the roles claim).
 */
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
