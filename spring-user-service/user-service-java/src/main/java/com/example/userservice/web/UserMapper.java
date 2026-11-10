package com.example.userservice.web;

import org.jspecify.annotations.Nullable;

import com.example.userservice.domain.Address;
import com.example.userservice.domain.NewUser;
import com.example.userservice.domain.User;
import com.example.userservice.domain.UserChanges;
import com.example.userservice.web.dto.AddressDto;
import com.example.userservice.web.dto.CreateUserRequest;
import com.example.userservice.web.dto.UpdateProfileRequest;
import com.example.userservice.web.dto.UpdateUserRequest;
import com.example.userservice.web.dto.UserResponse;

/**
 * Request DTO -> domain input, domain -> response DTO, written by hand. MapStruct generates this kind of code
 * for you; by hand it is a dozen lines per type and a compile error when a field is added and forgotten.
 */
final class UserMapper {

    private UserMapper() {
    }

    static NewUser toNewUser(CreateUserRequest request) {
        return new NewUser(request.username(), request.email(), request.displayName(),
                request.phone(), toAddress(request.address()));
    }

    static UserChanges toChanges(UpdateUserRequest request) {
        return new UserChanges(request.email(), request.displayName(), request.phone(),
                toAddress(request.address()), request.status());
    }

    /** No status: a user editing their own profile cannot change it. */
    static UserChanges toChanges(UpdateProfileRequest request) {
        return new UserChanges(request.email(), request.displayName(), request.phone(),
                toAddress(request.address()), null);
    }

    static UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username(), user.email(), user.displayName(),
                user.phone(), toDto(user.address()), user.status(), user.createdAt(), user.updatedAt());
    }

    private static @Nullable Address toAddress(@Nullable AddressDto dto) {
        return dto == null ? null : new Address(dto.street(), dto.city(), dto.postalCode(), dto.country());
    }

    private static @Nullable AddressDto toDto(@Nullable Address address) {
        return address == null ? null : new AddressDto(address.street(), address.city(), address.postalCode(), address.country());
    }
}
