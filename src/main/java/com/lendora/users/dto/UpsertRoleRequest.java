package com.lendora.users.dto;

import java.util.Set;

public record UpsertRoleRequest(
    String code,
    String name,
    String description,
    Set<String> permissions
) {
}