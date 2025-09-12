package com.lendora.users.service;

import com.lendora.users.dto.*;
import com.lendora.users.entity.Permission;
import com.lendora.users.mapper.PermissionMapper;
import com.lendora.users.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository repo;

    @Transactional
    public PermissionDTO create(PermissionDTO dto) {
        if (repo.existsByCodeIgnoreCase(dto.code()))
            throw new IllegalArgumentException("El código ya existe: " + dto.code());
        var p = new Permission();
        PermissionMapper.applyCreate(p, dto);
        return PermissionMapper.toDTO(repo.save(p));
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
        PermissionMapper.applyUpdate(p, dto);
        return PermissionMapper.toDTO(repo.save(p));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}