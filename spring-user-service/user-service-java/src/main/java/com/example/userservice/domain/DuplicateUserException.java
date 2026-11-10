package com.example.userservice.domain;

/** Another user already has this username or email (what a unique index violation is in SQL). */
public class DuplicateUserException extends RuntimeException {

    private final String field;

    public DuplicateUserException(String field, String value) {
        super("A user with this " + field + " already exists: " + value);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
