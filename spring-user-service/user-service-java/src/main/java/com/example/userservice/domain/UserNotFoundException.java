package com.example.userservice.domain;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    private UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException withId(UUID id) {
        return new UserNotFoundException("No user with id " + id);
    }

    public static UserNotFoundException withUsername(String username) {
        return new UserNotFoundException("No user profile for '" + username + "'");
    }
}
