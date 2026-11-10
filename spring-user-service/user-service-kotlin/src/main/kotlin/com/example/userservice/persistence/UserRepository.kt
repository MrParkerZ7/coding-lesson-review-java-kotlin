package com.example.userservice.persistence

import com.example.userservice.domain.User
import java.util.UUID

/**
 * The seam to the database. Today InMemoryUserRepository; for SQL, a Spring Data JPA or JDBC repository
 * (or an adapter around one) takes its place and nothing above this interface changes.
 * "Not found" is a null return, where the Java twin returns Optional.
 */
interface UserRepository {
    fun findById(id: UUID): User?

    fun findByUsername(username: String): User?

    fun findAll(): List<User>

    /** Insert or update by id. Throws DuplicateUserException when another user has the username or email. */
    fun save(user: User): User

    /** @return true when a user was deleted, false when there was none with this id */
    fun deleteById(id: UUID): Boolean
}
