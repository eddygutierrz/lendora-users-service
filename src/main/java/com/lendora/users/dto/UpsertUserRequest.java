package com.lendora.users.dto;

import java.util.List;
import java.util.Set;

import com.lendora.users.enums.UserStatus;

public record UpsertUserRequest(
    String username,
    String password,  // plain; en service -> BCrypt
    String firstname,
    String lastname,
    String email,
    String phone,
    String office,
    List<String> accessibleOffices,
    Set<String> roles,
    UserStatus status
) {
}