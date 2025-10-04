package com.lendora.users.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.lendora.users.dto.ChangePasswordRequest;
import com.lendora.users.dto.UpsertUserRequest;
import com.lendora.users.dto.UserAuthDTO;
import com.lendora.users.dto.UserDTO;

public interface UserService {
    UserDTO create(UpsertUserRequest req);
    UserDTO update(Long userId, UpsertUserRequest req);
    UserDTO getByUsername(String username);
    UserAuthDTO getAuthByUsername(String username); // para Auth MS
    Page<UserDTO> list(Pageable pageable);
    void activate(Long userId);
    void deactivate(Long userId);
    void changePassword(Long userId, ChangePasswordRequest req);
    UserDTO getById(Long userId);
    void markLogin(String username);
}