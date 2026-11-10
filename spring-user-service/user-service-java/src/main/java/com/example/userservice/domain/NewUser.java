package com.example.userservice.domain;

import org.jspecify.annotations.Nullable;

/** What the service needs to create a user. The id, status and timestamps are the service's to decide. */
public record NewUser(
        String username,
        String email,
        String displayName,
        @Nullable String phone,
        @Nullable Address address) {
}
