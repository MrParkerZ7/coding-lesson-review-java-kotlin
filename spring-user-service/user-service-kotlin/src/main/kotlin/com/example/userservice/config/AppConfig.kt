package com.example.userservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration(proxyBeanMethods = false)
class AppConfig {

    /** Injected instead of calling Instant.now(), so a test can freeze the time. */
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
