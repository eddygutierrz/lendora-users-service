package com.lendora.users.service;

import com.lendora.audit.core.AuditSupport;
import com.lendora.users.dto.*;
import com.lendora.users.entity.Permission;
import com.lendora.users.mapper.PermissionMapper;
import com.lendora.users.repository.PermissionRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lendora.common.exception.ResourceNotFoundException;

@Service 
public class PermissionService {
    @Autowired PermissionRepository repo;
    @Autowired AuditSupport audit;

    @Transactional
    public PermissionDTO create(PermissionDTO dto) {
        if (repo.existsByCodeIgnoreCase(dto.code()))
            throw new IllegalArgumentException("El código ya existe: " + dto.code());
        var p = new Permission();
        PermissionMapper.applyCreate(p, dto);
        PermissionDTO permission = PermissionMapper.toDTO(repo.save(p));
        audit.created("Permission", String.valueOf(permission.id()), PermissionDTO.class, permission);
        //audit.created("User", String.valueOf(saved.getId()), User.class, saved);
        return permission;
    }

    @Transactional(readOnly = true)
    public Page<PermissionDTO> list(String q, Pageable pageable) {
        var page = (q == null || q.isBlank())
                ? repo.findAll(pageable)
                : repo.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, pageable);
        return page.map(PermissionMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public PermissionDTO get(Long id) {
        var p = repo.findById(id).orElseThrow();
        return PermissionMapper.toDTO(p);
    }

    @Transactional
    public PermissionDTO update(Long id, PermissionDTO dto) {
        var p = repo.findById(id).orElseThrow();
        PermissionDTO before = PermissionMapper.toDTO(p);
        PermissionMapper.applyUpdate(p, dto);
        PermissionDTO after = PermissionMapper.toDTO(repo.save(p));
        audit.updated("Permission", String.valueOf(id), PermissionDTO.class, before, after);
        return after;
    }

    @Transactional
    public void delete(Long id) {
        Permission p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + id));
        repo.delete(p);
        audit.deleted("Permission", String.valueOf(id), PermissionDTO.class, PermissionMapper.toDTO(p));
    }
}