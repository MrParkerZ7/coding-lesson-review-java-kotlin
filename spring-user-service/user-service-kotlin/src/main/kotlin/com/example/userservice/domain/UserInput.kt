package com.example.userservice.domain

/** What the service needs to create a user. The id, status and timestamps are the service's to decide. */
data class NewUser(
    val username: String,
    val email: String,
    val displayName: String,
    val phone: String? = null,
    val address: Address? = null,
)

/** A partial edit: a null field means "leave it as it is". (There is no way to clear a field, see the README.) */
data class UserChanges(
    val email: String? = null,
    val displayName: String? = null,
    val phone: String? = null,
    val address: Address? = null,
    val status: UserStatus? = null,
) {
    val isEmpty: Boolean
        get() = email == null && displayName == null && phone == null && address == null && status == null
}
