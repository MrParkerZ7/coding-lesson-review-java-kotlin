package com.example.userservice.domain;

public class EmptyUpdateException extends RuntimeException {

    public EmptyUpdateException() {
        super("Nothing to update: send at least one field to change");
    }
}
