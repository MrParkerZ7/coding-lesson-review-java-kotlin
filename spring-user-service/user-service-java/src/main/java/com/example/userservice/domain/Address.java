package com.example.userservice.domain;

/** A postal address. Either a user has a whole one or none at all. */
public record Address(String street, String city, String postalCode, String country) {
}
