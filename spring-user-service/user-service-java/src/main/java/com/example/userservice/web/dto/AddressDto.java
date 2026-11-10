package com.example.userservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** The JSON shape of an address, in requests and responses alike. */
public record AddressDto(
        @NotBlank @Size(max = 200) String street,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 20) String postalCode,
        @NotNull @Pattern(regexp = "^[A-Z]{2}$", message = "must be a 2-letter ISO country code, e.g. TH") String country) {
}
