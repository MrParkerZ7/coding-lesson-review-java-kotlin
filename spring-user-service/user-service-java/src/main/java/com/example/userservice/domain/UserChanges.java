package com.example.userservice.domain;

import org.jspecify.annotations.Nullable;

/** A partial edit: a null field means "leave it as it is". (There is no way to clear a field, see the README.) */
public record UserChanges(
        @Nullable String email,
        @Nullable String displayName,
        @Nullable String phone,
        @Nullable Address address,
        @Nullable UserStatus status) {

    public boolean isEmpty() {
        return email == null && displayName == null && phone == null && address == null && status == null;
    }
}
