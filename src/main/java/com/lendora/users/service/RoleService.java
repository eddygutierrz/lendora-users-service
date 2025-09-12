package com.lendora.users.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lendora.users.dto.RoleDTO;
import com.lendora.users.dto.UpsertRoleRequest;
import com.lendora.users.entity.Permission;
import com.lendora.users.entity.Role;
import com.lendora.users.mapper.RoleMapper;
import com.lendora.users.repository.PermissionRepository;
import com.lendora.users.repository.RoleRepository;
import com.lendora.common.exception.*;


@Service
@Transactional
public class RoleService {
    @Autowired private RoleRepository roles;
    @Autowired private PermissionRepository perms;

    @Transactional(readOnly = true)
    public Page<RoleDTO> list(String q, Pageable pageable) {
        return roles.search(q, pageable).map(RoleMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public RoleDTO get(Long id) {
        Role r = roles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
        return RoleMapper.toDTO(r);
    }

    public RoleDTO create(UpsertRoleRequest req) {
        validateReq(req, true);

        if (roles.existsByCode(req.code()))
            throw new ConflictException("La clave de rol ya existe: " + req.code());

        Role r = new Role();
        r.setCode(req.code().trim());
        r.setName(req.name().trim());
        r.setDescription(req.description());

        // mapear permissionCodes -> Permission entities
        setPermissionsFromCodes(r, req.permissions());

        r = roles.save(r);
        return RoleMapper.toDTO(r);
    }

    public RoleDTO update(Long id, UpsertRoleRequest req) {
        validateReq(req, false);

        Role r = roles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));

        // si cambia code, valida unicidad
        String newCode = req.code().trim();
        if (!r.getCode().equalsIgnoreCase(newCode) && roles.existsByCode(newCode)) {
            throw new ConflictException("La clave de rol ya existe: " + newCode);
        }

        r.setCode(newCode);
        r.setName(req.name().trim());
        r.setDescription(req.description());

        setPermissionsFromCodes(r, req.permissions());

        r = roles.save(r);
        return RoleMapper.toDTO(r);
    }

    public void delete(Long id) {
        Role r = roles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
        roles.delete(r);
    }

    // Helpers

    private void validateReq(UpsertRoleRequest req, boolean creating) {
        if (req == null) throw new BadRequestException("Payload requerido");
        if (req.code() == null || req.code().isBlank())
            throw new BadRequestException("code es requerido");
        if (req.name() == null || req.name().isBlank())
            throw new BadRequestException("name es requerido");
        if (req.permissions() == null)
            throw new BadRequestException("permissionCodes no puede ser null (usa lista vacía)");
    }

    private void setPermissionsFromCodes(Role r, Set<String> codes) {
        // normaliza lista
        Set<String> list = Optional.ofNullable(codes).orElseGet(Set::of);
        if (list.isEmpty()) {
            r.getPermissions().clear();
            return;
        }

        List<Permission> found = perms.findByCodeIn(list);
        // Verifica faltantes (para feedback temprano)
        Set<String> foundCodes = new HashSet<>();
        for (Permission p : found) foundCodes.add(p.getCode());
        List<String> missing = new ArrayList<>();
        for (String c : list) if (!foundCodes.contains(c)) missing.add(c);
        if (!missing.isEmpty())
            throw new BadRequestException("Permisos inexistentes: " + String.join(", ", missing));

        r.getPermissions().clear();
        r.getPermissions().addAll(found);
    }
}