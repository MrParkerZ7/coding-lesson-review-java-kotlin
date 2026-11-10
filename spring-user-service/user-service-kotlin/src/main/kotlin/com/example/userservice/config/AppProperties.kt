package com.example.userservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

/** The "app.*" block of application.yml, bound by constructor into an immutable data class. A Kotlin default replaces @DefaultValue. */
@ConfigurationProperties(prefix = "app")
data class AppProperties(val seedDemoUsers: Boolean = false)
