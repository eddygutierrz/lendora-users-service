package com.lendora.users.dto;

public record ChangePasswordRequest(
    String currentPassword,   // opcional si solo admin cambia; si es self-service: requerido
    String newPassword
) {
}