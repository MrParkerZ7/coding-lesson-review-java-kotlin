package com.example.userservice.web

import com.example.userservice.domain.Address
import com.example.userservice.domain.NewUser
import com.example.userservice.domain.User
import com.example.userservice.domain.UserChanges
import com.example.userservice.web.dto.AddressDto
import com.example.userservice.web.dto.CreateUserRequest
import com.example.userservice.web.dto.UpdateProfileRequest
import com.example.userservice.web.dto.UpdateUserRequest
import com.example.userservice.web.dto.UserResponse

// Request DTO -> domain input, domain -> response DTO, written by hand as extension functions:
// `request.toNewUser()` reads left to right and needs no mapper class. MapStruct is the Java way to generate this.

internal fun CreateUserRequest.toNewUser() = NewUser(username, email, displayName, phone, address?.toDomain())

internal fun UpdateUserRequest.toChanges() = UserChanges(email, displayName, phone, address?.toDomain(), status)

/** No status: a user editing their own profile cannot change it. */
internal fun UpdateProfileRequest.toChanges() = UserChanges(email, displayName, phone, address?.toDomain(), status = null)

internal fun User.toResponse() =
    UserResponse(id, username, email, displayName, phone, address?.toDto(), status, createdAt, updatedAt)

private fun AddressDto.toDomain() = Address(street, city, postalCode, country)

private fun Address.toDto() = AddressDto(street, city, postalCode, country)
