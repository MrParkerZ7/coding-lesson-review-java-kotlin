package com.example.userservice.domain

import java.time.Instant
import java.util.UUID

enum class UserStatus { ACTIVE, DISABLED }

/** A postal address. Either a user has a whole one or none at all. */
data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
)

/**
 * A user profile as the service stores it. Immutable: an edit builds a new User with [withChanges].
 * Nullability is in the types: `String` can never be null, `String?` can. No annotations, no package-info.
 */
data class User(
    val id: UUID,
    val username: String,
    val email: String,
    val displayName: String,
    val phone: String?,
    val address: Address?,
    val status: UserStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /** A copy with the non-null fields of [changes] applied. id, username and createdAt never change. */
    fun withChanges(changes: UserChanges, now: Instant): User = copy(
        email = changes.email ?: email,
        displayName = changes.displayName ?: displayName,
        phone = changes.phone ?: phone,
        address = changes.address ?: address,
        status = changes.status ?: status,
        updatedAt = now,
    )
}
