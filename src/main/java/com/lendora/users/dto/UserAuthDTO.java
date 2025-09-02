package com.lendora.users.dto;

import java.util.Set;

import com.lendora.users.enums.UserStatus;

public record UserAuthDTO(
    Long id,
    String username,
    String password,     // BCrypt hash
    Set<String> roles,   // e.g. ["ADMIN","AUDITOR"]
    UserStatus status    // ACTIVE / INACTIVE
) {}