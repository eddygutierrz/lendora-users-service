// src/main/java/com/lendora/auth/controller/PermissionController.java
package com.lendora.users.controller;

import com.lendora.users.dto.*;
import com.lendora.users.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/users/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService service;

    @PostMapping
    @PreAuthorize("hasAuthority('permissions.create')")
    public ResponseEntity<PermissionDTO> create(@RequestBody @Valid PermissionDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PreAuthorize("hasAuthority('permissions.view')")
    @GetMapping
    public ResponseEntity<Page<PermissionDTO>> list(
            @RequestParam(required = false) String q, Pageable pageable) {
        return ResponseEntity.ok(service.list(q, pageable));
    }

    @PreAuthorize("hasAuthority('permissions.view')")
    @GetMapping("/{id}")
    public ResponseEntity<PermissionDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions.edit')")
    public ResponseEntity<PermissionDTO> update(@PathVariable Long id,
                                                @RequestBody @Valid PermissionDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}