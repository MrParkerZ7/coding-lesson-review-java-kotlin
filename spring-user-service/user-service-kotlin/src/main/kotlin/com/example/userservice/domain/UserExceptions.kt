package com.example.userservice.domain

import java.util.UUID

class UserNotFoundException private constructor(message: String) : RuntimeException(message) {
    companion object {
        fun withId(id: UUID) = UserNotFoundException("No user with id $id")
        fun withUsername(username: String) = UserNotFoundException("No user profile for '$username'")
    }
}

/** Another user already has this username or email (what a unique index violation is in SQL). */
class DuplicateUserException(val field: String, value: String) :
    RuntimeException("A user with this $field already exists: $value")

class EmptyUpdateException : RuntimeException("Nothing to update: send at least one field to change")
