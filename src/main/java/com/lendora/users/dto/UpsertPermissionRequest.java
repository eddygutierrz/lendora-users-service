package com.lendora.users.dto;

public record UpsertPermissionRequest(
    String code,
    String name,
    String description// Lista de códigos de permisos
) {}