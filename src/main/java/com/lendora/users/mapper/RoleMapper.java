package com.lendora.users.mapper;

import com.lendora.users.dto.RoleDTO;
import com.lendora.users.entity.Role;

import java.util.stream.Collectors;

public final class RoleMapper {
    private RoleMapper() {}

    public static RoleDTO toDTO(Role r) {
        return new RoleDTO(
            r.getId(),
            r.getCode(),
            r.getName(),
            r.getDescription(),
            r.getPermissions().stream().map(p -> p.getCode()).collect(Collectors.toSet())
        );
    }
}