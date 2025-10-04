package com.lendora.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lendora.common.exception.BadRequestException;
import com.lendora.users.dto.ResolvePermissionsRequest;
import com.lendora.users.dto.ResolvePermissionsResponse;
import com.lendora.users.dto.RoleDTO;
import com.lendora.users.dto.UpsertRoleRequest;
import com.lendora.users.service.RoleService;

@RestController
@RequestMapping("/users/roles")
public class RoleController {
    @Autowired private RoleService service;

    @PreAuthorize("hasAuthority('roles.view')")
    @GetMapping
    public Page<RoleDTO> list(@RequestParam(required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @PreAuthorize("hasAuthority('roles.view')")
    @GetMapping("/all")
    public java.util.List<RoleDTO> list() {
        return service.list();
    }

    @PreAuthorize("hasAuthority('roles.view')")
    @GetMapping("/{id}")
    public RoleDTO get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles.create')")
    public ResponseEntity<RoleDTO> create(@RequestBody UpsertRoleRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.edit')")
    public RoleDTO update(@PathVariable Long id, @RequestBody UpsertRoleRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/resolve-permissions")
    public ResolvePermissionsResponse resolve(@RequestBody ResolvePermissionsRequest req) {
        if (req == null || req.roles() == null) throw new BadRequestException("roles requerido");
        var perms = service.resolvePermissions(req.roles()); // dev: Set<String>
        return new ResolvePermissionsResponse(perms);
    }
}