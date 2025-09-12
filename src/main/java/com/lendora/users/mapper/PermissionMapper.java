// src/main/java/com/lendora/auth/mapper/PermissionMapper.java
package com.lendora.users.mapper;

import com.lendora.users.dto.*;
import com.lendora.users.entity.Permission;

public final class PermissionMapper {
    private PermissionMapper(){}

    public static PermissionDTO toDTO(Permission p) {
        return new PermissionDTO(p.getId(), p.getCode(), p.getName(), p.getDescription());
    }

    public static void applyCreate(Permission p, PermissionDTO dto) {
        p.setCode(dto.code().trim());
        p.setName(dto.name());
        p.setDescription(dto.description());
    }

    public static void applyUpdate(Permission p, PermissionDTO dto) {
        if (dto.description() != null) p.setDescription(dto.description());
        if (dto.name() != null) p.setName(dto.name());
        // Code is immutable
    }
}