package com.lendora.users.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import com.lendora.audit.api.Mask;
import com.lendora.users.enums.UserStatus;

public record UserDTO(
    Long id,
    String username,
    String firstname,
    String lastname,
    @Mask(Mask.MaskType.EMAIL)
    String email,
    @Mask(Mask.MaskType.PHONE)
    String phone,
    String office,
    List<String> accessibleOffices,
    Set<String> roles,
    UserStatus status,
    OffsetDateTime lastLoginAt
) {
}