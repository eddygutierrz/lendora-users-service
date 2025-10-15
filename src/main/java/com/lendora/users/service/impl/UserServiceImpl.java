package com.lendora.users.service.impl;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lendora.audit.core.AuditSupport;
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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository repo;
    @Autowired private  BCryptPasswordEncoder passwordEncoder;
    @Autowired private AuditSupport audit;
    
    @Override
    public UserDTO create(UpsertUserRequest req){
        if (repo.existsByUsername(req.username())) {
            throw new ConflictException("El username ya existe: " + req.username());
        }
        User u = new User();
        UserMapper.apply(u, req, passwordEncoder::encode);
        if (u.getStatus() == null) u.setStatus(UserStatus.ACTIVE);

        User saved = repo.save(u);
        UserDTO dto = UserMapper.toDTO(saved);
        audit.created("User", String.valueOf(saved.getId()), User.class, saved);
        return dto;
    }
    
    @Override
    public UserDTO update(Long userId, UpsertUserRequest req){
        User entity = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        // Snapshot BEFORE (copia independiente)
        UserDTO before = UserMapper.toDTO(entity); 
        // Validar username único si ha cambiado
        if (!Objects.equals(entity.getUsername(), req.username()) && repo.existsByUsername(req.username())) {
            throw new ConflictException("El username ya existe: " + req.username());
        }
        
        // Actualizar campos y codificar password si se provee
        UserMapper.apply(entity, req, p -> (p == null || p.isBlank()) ? entity.getPassword() : passwordEncoder.encode(p));

        UserDTO saved = UserMapper.toDTO(repo.save(entity));
        audit.updated("User", String.valueOf(entity.getId()), UserDTO.class, before, saved);
        return saved;
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
        User entity = repo.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        // 1) Snapshot BEFORE (copia independiente)
        UserDTO before = UserMapper.toDTO(entity); // o una copia profunda del entity

        // 2) Mutación y persistencia
        entity.setStatus(UserStatus.ACTIVE);
        User saved = repo.save(entity);

        // 3) Snapshot AFTER
        UserDTO after = UserMapper.toDTO(saved);

        // 4) Publicar evento
        audit.updated("User", String.valueOf(saved.getId()), UserDTO.class, before, after);
    }
    
    @Override
    public void deactivate(Long userId){
        User entity = repo.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        // 1) Snapshot BEFORE (copia independiente)
        UserDTO before = UserMapper.toDTO(entity); // o una copia profunda del entity

        // 2) Mutación y persistencia
        entity.setStatus(UserStatus.INACTIVE);
        User saved = repo.save(entity);

        // 3) Snapshot AFTER
        UserDTO after = UserMapper.toDTO(saved);

        // 4) Publicar evento
        audit.updated("User", String.valueOf(saved.getId()), UserDTO.class, before, after);
    }
    
    @Override
    public void changePassword(Long userId, ChangePasswordRequest req){
        User entity = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        // Validar si la contraseña nueva es provista
        if (req == null || req.newPassword() == null || req.newPassword().isBlank()) {
            throw new BadRequestException("La nueva contraseña es obligatoria");
        }
        // Validar si la nueva contraseña es diferente a la actual
        if (passwordEncoder.matches(req.newPassword(), entity.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }
        // Si quieres exigir currentPassword (para self-service):
        if (req.currentPassword() != null && !req.currentPassword().isBlank()) {
            if (!passwordEncoder.matches(req.currentPassword(), entity.getPassword())) {
                throw new BadRequestException("La contraseña actual no coincide");
            }
        }
        // Snapshot BEFORE (copia independiente)
        UserDTO before = UserMapper.toDTO(entity);
        entity.setPassword(passwordEncoder.encode(req.newPassword()));// Actualizar password
        UserDTO saved = UserMapper.toDTO(repo.save(entity));
        audit.updated("User", String.valueOf(entity.getId()), UserDTO.class, before, saved);
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

    @Override
    public void delete(Long userId) {
        User entity = repo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        repo.delete(entity);
        audit.deleted("User", String.valueOf(entity.getId()), UserDTO.class, UserMapper.toDTO(entity));
    }
}