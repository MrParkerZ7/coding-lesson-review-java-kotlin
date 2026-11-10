package com.example.userservice.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.userservice.domain.User;
import com.example.userservice.domain.UserStatus;
import com.example.userservice.service.UserService;
import com.example.userservice.web.dto.CreateUserRequest;
import com.example.userservice.web.dto.UpdateProfileRequest;
import com.example.userservice.web.dto.UpdateUserRequest;
import com.example.userservice.web.dto.UserResponse;

/**
 * The HTTP edge: maps JSON to domain types and back, and nothing else. Who may call what is decided twice:
 * SecurityConfig checks the token's scope (users.read / users.write), and the @PreAuthorize here checks the role.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')") // every endpoint is admin-only unless its own @PreAuthorize says otherwise
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User created = users.create(UserMapper.toNewUser(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(UserMapper.toResponse(created));
    }

    /** {@code @Nullable} on a parameter makes it optional: no status or q in the URL arrives as null. */
    @GetMapping
    public List<UserResponse> list(@RequestParam @Nullable UserStatus status, @RequestParam("q") @Nullable String query) {
        return users.search(status, query).stream().map(UserMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return UserMapper.toResponse(users.get(id));
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return UserMapper.toResponse(users.update(id, UserMapper.toChanges(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        users.delete(id);
    }

    // "me" is the caller's own profile: the username is the token's subject, so nobody can name somebody else's

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserMapper.toResponse(users.getByUsername(username(jwt)));
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        return UserMapper.toResponse(users.updateByUsername(username(jwt), UserMapper.toChanges(request)));
    }

    /** "sub" is optional in a JWT, so getSubject() may be null. Java does not make us check; a token without one is a 401. */
    private static String username(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new InvalidBearerTokenException("The token has no subject");
        }
        return subject;
    }
}
