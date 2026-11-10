package com.example.userservice.service

import com.example.userservice.domain.Address
import com.example.userservice.domain.DuplicateUserException
import com.example.userservice.domain.EmptyUpdateException
import com.example.userservice.domain.NewUser
import com.example.userservice.domain.User
import com.example.userservice.domain.UserChanges
import com.example.userservice.domain.UserNotFoundException
import com.example.userservice.domain.UserStatus
import com.example.userservice.persistence.InMemoryUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

/** No Spring here: the service is plain Kotlin, so it is built by hand and tested in milliseconds. */
class UserServiceTest {

    private val repository = InMemoryUserRepository()
    private val service = UserService(repository, Clock.fixed(CREATED, ZoneOffset.UTC))
    private val laterService = UserService(repository, Clock.fixed(EDITED, ZoneOffset.UTC))

    private fun newUser(username: String, email: String) = NewUser(username, email, "Display $username")

    @Test
    fun `create assigns id, status and timestamps`() {
        val user = service.create(newUser("carol", "carol@example.com"))

        assertThat(user.id).isNotNull()
        assertThat(user.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(user.createdAt).isEqualTo(CREATED)
        assertThat(user.updatedAt).isEqualTo(CREATED)
        assertThat(user.phone).isNull()
        assertThat(service.get(user.id)).isEqualTo(user)
    }

    @Test
    fun `duplicate username or email is rejected ignoring case`() {
        service.create(newUser("carol", "carol@example.com"))

        assertThatThrownBy { service.create(newUser("CAROL", "other@example.com")) }
            .isInstanceOf(DuplicateUserException::class.java)
            .hasFieldOrPropertyWithValue("field", "username")
        assertThatThrownBy { service.create(newUser("dave", "Carol@Example.com")) }
            .isInstanceOf(DuplicateUserException::class.java)
            .hasFieldOrPropertyWithValue("field", "email")
    }

    @Test
    fun `unknown id and unknown username are not found`() {
        assertThatThrownBy { service.get(UUID.randomUUID()) }.isInstanceOf(UserNotFoundException::class.java)
        assertThatThrownBy { service.getByUsername("nobody") }.isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `update changes only the given fields and bumps updatedAt`() {
        val before = service.create(
            NewUser("carol", "carol@example.com", "Carol", "+66811111111", Address("1 Main St", "Bangkok", "10110", "TH")),
        )

        val after = laterService.update(before.id, UserChanges(displayName = "Carol C.", status = UserStatus.DISABLED))

        assertThat(after.displayName).isEqualTo("Carol C.")
        assertThat(after.status).isEqualTo(UserStatus.DISABLED)
        assertThat(after.email).isEqualTo(before.email)
        assertThat(after.phone).isEqualTo(before.phone)
        assertThat(after.address).isEqualTo(before.address)
        assertThat(after.createdAt).isEqualTo(CREATED)
        assertThat(after.updatedAt).isEqualTo(EDITED)
    }

    @Test
    fun `update with no changes is rejected`() {
        val user = service.create(newUser("carol", "carol@example.com"))

        assertThatThrownBy { service.update(user.id, UserChanges()) }.isInstanceOf(EmptyUpdateException::class.java)
    }

    @Test
    fun `update to an email another user has is rejected`() {
        service.create(newUser("carol", "carol@example.com"))
        val dave = service.create(newUser("dave", "dave@example.com"))

        assertThatThrownBy { service.update(dave.id, UserChanges(email = "carol@example.com")) }
            .isInstanceOf(DuplicateUserException::class.java)
        assertThat(service.get(dave.id).email).isEqualTo("dave@example.com")
    }

    @Test
    fun `update keeping your own email is not a duplicate`() {
        val carol = service.create(newUser("carol", "carol@example.com"))

        val same = service.update(carol.id, UserChanges(email = "carol@example.com"))

        assertThat(same.email).isEqualTo("carol@example.com")
    }

    @Test
    fun `delete removes the user and a second delete is not found`() {
        val carol = service.create(newUser("carol", "carol@example.com"))

        service.delete(carol.id)

        assertThatThrownBy { service.get(carol.id) }.isInstanceOf(UserNotFoundException::class.java)
        assertThatThrownBy { service.delete(carol.id) }.isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `search filters by status and text and sorts by username`() {
        service.create(newUser("zed", "zed@example.com"))
        val amy = service.create(NewUser("amy", "amy@example.com", "Amy Adams"))
        service.create(newUser("bea", "bea@example.com"))
        service.update(amy.id, UserChanges(status = UserStatus.DISABLED))

        assertThat(service.search(null, null)).extracting<String> { it.username }.containsExactly("amy", "bea", "zed")
        assertThat(service.search(UserStatus.DISABLED, null)).extracting<String> { it.username }.containsExactly("amy")
        assertThat(service.search(null, "  ADAMS ")).extracting<String> { it.username }.containsExactly("amy")
        assertThat(service.search(UserStatus.ACTIVE, "amy")).isEmpty()
    }

    private companion object {
        val CREATED: Instant = Instant.parse("2026-01-01T10:00:00Z")
        val EDITED: Instant = Instant.parse("2026-01-02T10:00:00Z")
    }
}
