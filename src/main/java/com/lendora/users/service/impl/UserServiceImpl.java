package com.lendora.users.service.impl;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lendora.common.exception.BadRequestException;
import com.lendora.common.exception.ConflictException;
import com.lendora.common.exception.ResourceNotFoundException;
import com.lendora.users.dto.ChangePasswordRequest;
import com.lendora.users.dto.UpsertUserRequest;
import com.lendora.users.dto.UserAuthDTO;
import com.lendora.users.dto.UserDTO;
import com.lendora.users.entity.User;
import com.lendora.users.enums.UserStatus;
import com.lendora.users.repository.UserRepository;
import com.lendora.users.service.UserService;
import com.lendora.users.utils.UserMapper;

@Service
public class UserServiceImpl implements UserService {
    @Autowired private UserRepository repo;
    @Autowired private  BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public UserDTO create(UpsertUserRequest req){
        if (repo.existsByUsername(req.username())) {
            throw new ConflictException("El username ya existe: " + req.username());
        }
        User u = new User();
        // password es obligatoria en create
        UserMapper.apply(u, req, passwordEncoder::encode);
        if (u.getStatus() == null) u.setStatus(UserStatus.ACTIVE);
        return UserMapper.toDTO(repo.save(u));
    }
    
    @Override
    public UserDTO update(Long userId, UpsertUserRequest req){
        User u = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        // Si cambia username, validar disponibilidad
        if (!Objects.equals(u.getUsername(), req.username())
                && repo.existsByUsername(req.username())) {
            throw new ConflictException("El username ya existe: " + req.username());
        }

        // En update la password puede venir vacía (no cambia)
        UserMapper.apply(u, req, p -> (p == null || p.isBlank()) ? u.getPassword() : passwordEncoder.encode(p));
        return UserMapper.toDTO(repo.save(u));
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO getByUsername(String username){
        User u = repo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return UserMapper.toDTO(u);
    }
    
    @Override
    public UserAuthDTO getAuthByUsername(String username){
        User u = repo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return UserMapper.toAuthDTO(u);
    }
    
    @Override
    public Page<UserDTO> list(Pageable pageable){
        return repo.findAll(pageable).map(UserMapper::toDTO);
    }
    
    @Override
    public void activate(Long userId){
        User u = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        u.setStatus(UserStatus.ACTIVE);
        repo.save(u);
    }
    
    @Override
    public void deactivate(Long userId){
        User u = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        u.setStatus(UserStatus.INACTIVE);
        repo.save(u);
    }
    
    @Override
    public void changePassword(Long userId, ChangePasswordRequest req){
        if (req == null || req.newPassword() == null || req.newPassword().isBlank()) {
            throw new BadRequestException("La nueva contraseña es obligatoria");
        }
        User u = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        // Si quieres exigir currentPassword (para self-service):
        if (req.currentPassword() != null && !req.currentPassword().isBlank()) {
            if (!passwordEncoder.matches(req.currentPassword(), u.getPassword())) {
                throw new BadRequestException("La contraseña actual no coincide");
            }
        }

        // Podrías validar fortaleza aquí (mínimos, etc.)
        u.setPassword(passwordEncoder.encode(req.newPassword()));
        repo.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long userId){
        User u = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        return UserMapper.toDTO(u);
    }

    @Transactional
    public void markLogin(String username) {
        var u = repo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setLastLoginAt(OffsetDateTime.now());
        repo.save(u);
    }

}