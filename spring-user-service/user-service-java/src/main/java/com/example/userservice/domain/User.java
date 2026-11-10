package com.example.userservice.domain;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * A user profile as the service stores it. Immutable: an edit builds a new User with {@link #withChanges}.
 * The package is @NullMarked, so every component is non-null unless it says @Nullable.
 */
public record User(
        UUID id,
        String username,
        String email,
        String displayName,
        @Nullable String phone,
        @Nullable Address address,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt) {

    /** A copy with the non-null fields of {@code changes} applied. id, username and createdAt never change. */
    public User withChanges(UserChanges changes, Instant now) {
        return new User(
                id,
                username,
                changes.email() != null ? changes.email() : email,
                changes.displayName() != null ? changes.displayName() : displayName,
                changes.phone() != null ? changes.phone() : phone,
                changes.address() != null ? changes.address() : address,
                changes.status() != null ? changes.status() : status,
                createdAt,
                now);
    }
}
