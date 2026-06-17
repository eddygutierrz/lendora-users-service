package com.lendora.users.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lendora.users.dto.PermissionDependenciesGraphDTO;
import com.lendora.users.dto.PermissionDependencyDTO;
import com.lendora.users.service.PermissionDependencyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints del grafo de dependencias entre permisos.
 *
 * <pre>
 * GET    /permissions/dependencies          — devuelve el grafo agregado (roles.read)
 * POST   /permissions/dependencies          — crea una dependencia (roles.update)
 * DELETE /permissions/dependencies/{id}     — elimina una dependencia (roles.update)
 * </pre>
 *
 * Diseño: el grafo lo gobierna users-service (dueño único). Cada MS que
 * agregue permisos nuevos documenta sus dependencias en su README; el seed
 * vive en users-service.
 */
@RestController
@RequestMapping("/permissions/dependencies")
@RequiredArgsConstructor
public class PermissionDependencyController {

    private final PermissionDependencyService service;

    @GetMapping
    @PreAuthorize("hasAuthority('roles.read')")
    public ResponseEntity<PermissionDependenciesGraphDTO> graph() {
        return ResponseEntity.ok(service.graph());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles.update')")
    public ResponseEntity<PermissionDependencyDTO> create(@Valid @RequestBody PermissionDependencyDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.update')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
