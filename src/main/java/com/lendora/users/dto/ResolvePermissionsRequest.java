package com.lendora.users.dto;

import java.util.Set;

public record ResolvePermissionsRequest(
    Set<String> roles
) {}