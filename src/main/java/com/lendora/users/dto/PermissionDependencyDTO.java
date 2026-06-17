package com.lendora.users.dto;

import com.lendora.users.enums.DependencyKind;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload de alta/baja de una dependencia. Se reciben/devuelven los códigos
 * de permiso (no IDs) para que el front no dependa del ID interno.
 */
public record PermissionDependencyDTO(
    @NotBlank String permissionCode,
    @NotBlank String dependsOnCode,
    @NotNull  DependencyKind kind
) {}
