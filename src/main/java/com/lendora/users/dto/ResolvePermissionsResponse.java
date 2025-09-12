package com.lendora.users.dto;

import java.util.Set;

public record ResolvePermissionsResponse(
    Set<String> permissions
) {}