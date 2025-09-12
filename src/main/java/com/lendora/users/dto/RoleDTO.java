package com.lendora.users.dto;

import java.util.Set;

public record RoleDTO(
    Long id,
    String code,
    String name,
    String description,
    Set<String> permissions // Lista de códigos de permisos
) {
}