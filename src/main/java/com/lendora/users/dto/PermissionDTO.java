package com.lendora.users.dto;

public record PermissionDTO(
    Long id,
    String code,
    String name,
    String description
) {
}