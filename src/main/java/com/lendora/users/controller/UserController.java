package com.lendora.users.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lendora.users.dto.ChangePasswordRequest;
import com.lendora.users.dto.UpsertUserRequest;
import com.lendora.users.dto.UserAuthDTO;
import com.lendora.users.dto.UserDTO;
import com.lendora.users.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    
    @GetMapping("/auth/{username}")
    @PreAuthorize("hasAuthority('SCOPE_users-service.read_auth')")
    public ResponseEntity<UserAuthDTO> getAuthByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.getAuthByUsername(username));
    }

    @PatchMapping("/{username}/mark-login")
    @PreAuthorize("hasAuthority('SCOPE_users-service.read_auth')")
    public void markLogin(@PathVariable String username,
                        @RequestHeader(value="X-Client-IP", required=false) String ip) {
        service.markLogin(username);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users.create')")
    public ResponseEntity<UserDTO> create(@Validated @RequestBody UpsertUserRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users.edit')")
    public ResponseEntity<UserDTO> update(@PathVariable Long id,
                                            @Validated @RequestBody UpsertUserRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasAuthority('users.view')")
    public ResponseEntity<UserDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.getByUsername(username));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users.view')")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('users.view')")
    public ResponseEntity<Page<UserDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('users.activate_status')")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('users.deactivate_status')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/password")
    @PreAuthorize("hasAuthority('users.change_password')")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                                @Validated @RequestBody ChangePasswordRequest req) {
        service.changePassword(id, req);
        return ResponseEntity.noContent().build();
    }

}