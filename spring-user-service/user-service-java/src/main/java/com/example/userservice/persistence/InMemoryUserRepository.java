package com.example.userservice.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.userservice.domain.DuplicateUserException;
import com.example.userservice.domain.User;

/** A map standing in for the users table. synchronized = one writer at a time, so the unique check below is safe. */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> rows = new LinkedHashMap<>();

    @Override
    public synchronized Optional<User> findById(UUID id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) {
        return rows.values().stream().filter(user -> user.username().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public synchronized List<User> findAll() {
        return new ArrayList<>(rows.values());
    }

    @Override
    public synchronized User save(User user) {
        // what a unique index on lower(username) and lower(email) would enforce
        for (User other : rows.values()) {
            if (other.id().equals(user.id())) {
                continue;
            }
            if (other.username().equalsIgnoreCase(user.username())) {
                throw new DuplicateUserException("username", user.username());
            }
            if (other.email().equalsIgnoreCase(user.email())) {
                throw new DuplicateUserException("email", user.email());
            }
        }
        rows.put(user.id(), user);
        return user;
    }

    @Override
    public synchronized boolean deleteById(UUID id) {
        return rows.remove(id) != null;
    }
}
