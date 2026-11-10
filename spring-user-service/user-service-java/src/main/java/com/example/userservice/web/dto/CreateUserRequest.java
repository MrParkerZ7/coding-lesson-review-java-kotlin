package com.example.userservice.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

/** POST /api/users. A missing required field reaches the record as null, and bean validation turns that into a 400. */
public record CreateUserRequest(
        @NotNull @Pattern(regexp = ValidationRules.USERNAME, message = "must be 3-32 characters: a-z, 0-9, dot, underscore or dash") String username,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String displayName,
        @Nullable @Pattern(regexp = ValidationRules.PHONE, message = "must be 8-15 digits, optionally starting with +") String phone,
        @Nullable @Valid AddressDto address) {
}
