package com.example.userservice.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class AppConfig {

    /** Injected instead of calling Instant.now(), so a test can freeze the time. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
