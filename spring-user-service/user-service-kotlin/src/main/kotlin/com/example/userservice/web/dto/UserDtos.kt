package com.example.userservice.web.dto

import com.example.userservice.domain.UserStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

/** Regexes shared by the request classes. `const val` is a compile-time constant, which annotation values require. */
object ValidationRules {
    const val USERNAME = "^[a-z0-9._-]{3,32}\$"
    const val PHONE = "^\\+?[0-9]{8,15}\$"

    /** For optional text in an edit: null is fine (= unchanged), but "" or "   " is not. */
    const val NOT_BLANK = ".*\\S.*"
}

/** The JSON shape of an address, in requests and responses alike. */
data class AddressDto(
    @NotBlank @Size(max = 200) val street: String,
    @NotBlank @Size(max = 100) val city: String,
    @NotBlank @Size(max = 20) val postalCode: String,
    @Pattern(regexp = "^[A-Z]{2}\$", message = "must be a 2-letter ISO country code, e.g. TH") val country: String,
)

/**
 * POST /api/users. Required fields are non-null Kotlin types, so there is no @NotNull: a missing or null field
 * never reaches bean validation, Jackson's Kotlin module rejects the body first (see ApiExceptionHandler).
 * The cost of that: the first missing field is reported, not all of them as in the Java twin.
 */
data class CreateUserRequest(
    @Pattern(regexp = ValidationRules.USERNAME, message = "must be 3-32 characters: a-z, 0-9, dot, underscore or dash")
    val username: String,
    @NotBlank @Email @Size(max = 254) val email: String,
    @NotBlank @Size(max = 100) val displayName: String,
    @Pattern(regexp = ValidationRules.PHONE, message = "must be 8-15 digits, optionally starting with +")
    val phone: String? = null,
    @Valid val address: AddressDto? = null,
)

/**
 * PATCH /api/users/{id} (admin). Every field is optional: null or absent = leave it alone.
 * Bean validation skips null, so each rule below only applies to a field that was sent.
 */
data class UpdateUserRequest(
    @Email @Size(max = 254) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank")
    val email: String? = null,
    @Size(max = 100) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank")
    val displayName: String? = null,
    @Pattern(regexp = ValidationRules.PHONE, message = "must be 8-15 digits, optionally starting with +")
    val phone: String? = null,
    @Valid val address: AddressDto? = null,
    val status: UserStatus? = null,
)

/**
 * PATCH /api/users/me. The same as UpdateUserRequest minus "status": a user must not be able to re-enable
 * themselves. The field is not there, and the server rejects unknown JSON fields, so sending it is a 400.
 */
data class UpdateProfileRequest(
    @Email @Size(max = 254) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank")
    val email: String? = null,
    @Size(max = 100) @Pattern(regexp = ValidationRules.NOT_BLANK, message = "must not be blank")
    val displayName: String? = null,
    @Pattern(regexp = ValidationRules.PHONE, message = "must be 8-15 digits, optionally starting with +")
    val phone: String? = null,
    @Valid val address: AddressDto? = null,
)

/** What the API returns. Null fields are left out of the JSON (spring.jackson.default-property-inclusion=non_null). */
data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val displayName: String,
    val phone: String?,
    val address: AddressDto?,
    val status: UserStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)
