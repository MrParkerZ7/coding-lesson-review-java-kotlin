package com.example.userservice.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.userservice.domain.User;

/**
 * The seam to the database. Today InMemoryUserRepository; for SQL, a Spring Data JPA or JDBC repository
 * (or an adapter around one) takes its place and nothing above this interface changes.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    /**
     * Insert or update by id.
     *
     * @throws com.example.userservice.domain.DuplicateUserException when another user has the username or email
     */
    User save(User user);

    /** @return true when a user was deleted, false when there was none with this id */
    boolean deleteById(UUID id);
}
