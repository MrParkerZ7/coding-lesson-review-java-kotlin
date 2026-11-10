package com.example.userservice.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import com.example.userservice.domain.EmptyUpdateException;
import com.example.userservice.domain.NewUser;
import com.example.userservice.domain.User;
import com.example.userservice.domain.UserChanges;
import com.example.userservice.domain.UserNotFoundException;
import com.example.userservice.domain.UserStatus;
import com.example.userservice.persistence.UserRepository;

/** The use cases. Knows nothing about HTTP or security: it takes and returns domain types and throws domain exceptions. */
@Service
public class UserService {

    private final UserRepository users;
    private final Clock clock;

    // one constructor = Spring injects it, no @Autowired needed
    public UserService(UserRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    public User create(NewUser newUser) {
        Instant now = clock.instant();
        User user = new User(UUID.randomUUID(), newUser.username(), newUser.email(), newUser.displayName(),
                newUser.phone(), newUser.address(), UserStatus.ACTIVE, now, now);
        return users.save(user);
    }

    public User get(UUID id) {
        return users.findById(id).orElseThrow(() -> UserNotFoundException.withId(id));
    }

    public User getByUsername(String username) {
        return users.findByUsername(username).orElseThrow(() -> UserNotFoundException.withUsername(username));
    }

    /** All users, optionally only one status and/or whose username, display name or email contains {@code query}. */
    public List<User> search(@Nullable UserStatus status, @Nullable String query) {
        String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return users.findAll().stream()
                .filter(user -> status == null || user.status() == status)
                .filter(user -> needle.isEmpty() || matches(user, needle))
                .sorted(Comparator.comparing(User::username))
                .toList();
    }

    public User update(UUID id, UserChanges changes) {
        if (changes.isEmpty()) {
            throw new EmptyUpdateException();
        }
        User updated = get(id).withChanges(changes, clock.instant());
        return users.save(updated);
    }

    public User updateByUsername(String username, UserChanges changes) {
        return update(getByUsername(username).id(), changes);
    }

    public void delete(UUID id) {
        if (!users.deleteById(id)) {
            throw UserNotFoundException.withId(id);
        }
    }

    private static boolean matches(User user, String needle) {
        return user.username().toLowerCase(Locale.ROOT).contains(needle)
                || user.displayName().toLowerCase(Locale.ROOT).contains(needle)
                || user.email().toLowerCase(Locale.ROOT).contains(needle);
    }
}
