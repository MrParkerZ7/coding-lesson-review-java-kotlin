package com.example.userservice.service

import com.example.userservice.domain.EmptyUpdateException
import com.example.userservice.domain.NewUser
import com.example.userservice.domain.User
import com.example.userservice.domain.UserChanges
import com.example.userservice.domain.UserNotFoundException
import com.example.userservice.domain.UserStatus
import com.example.userservice.persistence.UserRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.UUID

/** The use cases. Knows nothing about HTTP or security: it takes and returns domain types and throws domain exceptions. */
@Service
class UserService(
    private val users: UserRepository, // the primary constructor is the injection point: no field, no @Autowired
    private val clock: Clock,
) {

    fun create(newUser: NewUser): User {
        val now = clock.instant()
        val user = User(
            id = UUID.randomUUID(),
            username = newUser.username,
            email = newUser.email,
            displayName = newUser.displayName,
            phone = newUser.phone,
            address = newUser.address,
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        return users.save(user)
    }

    // a null from the repository becomes the exception with ?: (the Java twin uses Optional.orElseThrow)
    fun get(id: UUID): User = users.findById(id) ?: throw UserNotFoundException.withId(id)

    fun getByUsername(username: String): User =
        users.findByUsername(username) ?: throw UserNotFoundException.withUsername(username)

    /** All users, optionally only one status and/or whose username, display name or email contains [query]. */
    fun search(status: UserStatus?, query: String?): List<User> {
        val needle = query?.trim()?.lowercase().orEmpty()
        return users.findAll()
            .filter { status == null || it.status == status }
            .filter { needle.isEmpty() || it.matches(needle) }
            .sortedBy { it.username }
    }

    fun update(id: UUID, changes: UserChanges): User {
        if (changes.isEmpty) throw EmptyUpdateException()
        return users.save(get(id).withChanges(changes, clock.instant()))
    }

    fun updateByUsername(username: String, changes: UserChanges): User = update(getByUsername(username).id, changes)

    fun delete(id: UUID) {
        if (!users.deleteById(id)) throw UserNotFoundException.withId(id)
    }

    private fun User.matches(needle: String): Boolean =
        username.lowercase().contains(needle) ||
            displayName.lowercase().contains(needle) ||
            email.lowercase().contains(needle)
}
