package com.lendora.users.dto;

import java.util.List;

public record MenuItemDTO(
    String label,
    String route,
    String icon,
    List<MenuItemDTO> children
) {}