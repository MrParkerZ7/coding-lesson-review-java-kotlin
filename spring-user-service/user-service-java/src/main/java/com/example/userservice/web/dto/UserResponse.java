package com.example.userservice.web.dto;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.example.userservice.domain.UserStatus;

/** What the API returns. Null fields are left out of the JSON (spring.jackson.default-property-inclusion=non_null). */
public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        @Nullable String phone,
        @Nullable AddressDto address,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
