package com.example.userservice.persistence

import com.example.userservice.domain.DuplicateUserException
import com.example.userservice.domain.User
import org.springframework.stereotype.Repository
import java.util.UUID

/** A map standing in for the users table. @Synchronized = one writer at a time, so the unique check below is safe. */
@Repository
class InMemoryUserRepository : UserRepository {

    private val rows = LinkedHashMap<UUID, User>()

    @Synchronized
    override fun findById(id: UUID): User? = rows[id]

    @Synchronized
    override fun findByUsername(username: String): User? =
        rows.values.firstOrNull { it.username.equals(username, ignoreCase = true) }

    @Synchronized
    override fun findAll(): List<User> = rows.values.toList()

    @Synchronized
    override fun save(user: User): User {
        // what a unique index on lower(username) and lower(email) would enforce
        for (other in rows.values) {
            if (other.id == user.id) continue
            if (other.username.equals(user.username, ignoreCase = true)) throw DuplicateUserException("username", user.username)
            if (other.email.equals(user.email, ignoreCase = true)) throw DuplicateUserException("email", user.email)
        }
        rows[user.id] = user
        return user
    }

    @Synchronized
    override fun deleteById(id: UUID): Boolean = rows.remove(id) != null
}
