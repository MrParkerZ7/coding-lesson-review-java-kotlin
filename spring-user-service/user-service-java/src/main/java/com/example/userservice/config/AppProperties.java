package com.example.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** The "app.*" block of application.yml, bound by constructor into an immutable record. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(@DefaultValue("false") boolean seedDemoUsers) {
}
