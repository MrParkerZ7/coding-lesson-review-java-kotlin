package com.example.userservice.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

/**
 * PATCH /api/users/me. The same as UpdateUserRequest minus "status": a user must not be able to re-enable
 * themselves. The field is not there, and the server rejects unknown JSON fields, so sending it is a 400.
 */
public record UpdateProfileRequest(
        @Nullable @Email @Size(max = 254) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank") String email,
        @Nullable @Size(max = 100) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank") String displayName,
        @Nullable @Pattern(regexp = ValidationRules.PHONE, message = "must be 8-15 digits, optionally starting with +") String phone,
        @Nullable @Valid AddressDto address) {
}
