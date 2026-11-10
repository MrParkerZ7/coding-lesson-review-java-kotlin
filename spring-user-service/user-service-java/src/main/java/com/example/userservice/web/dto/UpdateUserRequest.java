package com.example.userservice.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import com.example.userservice.domain.UserStatus;

/**
 * PATCH /api/users/{id} (admin). Every field is optional: null or absent = leave it alone.
 * Bean validation skips null, so each rule below only applies to a field that was sent.
 */
public record UpdateUserRequest(
        @Nullable @Email @Size(max = 254) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank") String email,
        @Nullable @Size(max = 100) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank") String displayName,
        @Nullable @Pattern(regexp = ValidationRules.PHONE, message = "must be 8-15 digits, optionally starting with +") String phone,
        @Nullable @Valid AddressDto address,
        @Nullable UserStatus status) {
}
