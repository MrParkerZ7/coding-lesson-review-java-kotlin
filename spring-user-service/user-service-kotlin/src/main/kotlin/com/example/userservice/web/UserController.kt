package com.example.userservice.web

import com.example.userservice.domain.UserStatus
import com.example.userservice.service.UserService
import com.example.userservice.web.dto.CreateUserRequest
import com.example.userservice.web.dto.UpdateProfileRequest
import com.example.userservice.web.dto.UpdateUserRequest
import com.example.userservice.web.dto.UserResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

/**
 * The HTTP edge: maps JSON to domain types and back, and nothing else. Who may call what is decided twice:
 * SecurityConfig checks the token's scope (users.read / users.write), and the @PreAuthorize here checks the role.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')") // every endpoint is admin-only unless its own @PreAuthorize says otherwise
class UserController(private val users: UserService) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val created = users.create(request.toNewUser())
        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id).toUri()
        return ResponseEntity.created(location).body(created.toResponse())
    }

    /** A nullable parameter type makes it optional: no status or q in the URL arrives as null. */
    @GetMapping
    fun list(@RequestParam status: UserStatus?, @RequestParam("q") query: String?): List<UserResponse> =
        users.search(status, query).map { it.toResponse() }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): UserResponse = users.get(id).toResponse()

    @PatchMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: UpdateUserRequest): UserResponse =
        users.update(id, request.toChanges()).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) = users.delete(id)

    // "me" is the caller's own profile: the username is the token's subject, so nobody can name somebody else's

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun me(@AuthenticationPrincipal jwt: Jwt): UserResponse = users.getByUsername(jwt.username).toResponse()

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun updateMe(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody request: UpdateProfileRequest): UserResponse =
        users.updateByUsername(jwt.username, request.toChanges()).toResponse()
}

/**
 * "sub" is optional in a JWT, so `jwt.subject` is a `String?` and the compiler makes us deal with it
 * (the Java twin compiles without a word). A token without one is a 401.
 */
private val Jwt.username: String
    get() = subject ?: throw InvalidBearerTokenException("The token has no subject")
