package com.lendora.users.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lendora.common.exception.ResourceNotFoundException;
import com.lendora.users.dto.PermissionDependenciesGraphDTO;
import com.lendora.users.dto.PermissionDependencyDTO;
import com.lendora.users.entity.Permission;
import com.lendora.users.entity.PermissionDependency;
import com.lendora.users.enums.DependencyKind;
import com.lendora.users.repository.PermissionDependencyRepository;
import com.lendora.users.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Gestiona el grafo de dependencias entre permisos.
 *
 * Reglas:
 *  - permissionCode y dependsOnCode deben existir en la tabla `permissions`.
 *  - No se permite dependencia consigo mismo.
 *  - El unique (permission, depends_on, kind) impide duplicados.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionDependencyService {

    private final PermissionDependencyRepository depRepo;
    private final PermissionRepository permRepo;

    @Transactional(readOnly = true)
    public PermissionDependenciesGraphDTO graph() {
        Map<String, List<String>> requires = new LinkedHashMap<>();
        Map<String, List<String>> implies  = new LinkedHashMap<>();

        for (PermissionDependency d : depRepo.findAllWithRefs()) {
            String src = d.getPermission().getCode();
            String dst = d.getDependsOn().getCode();
            Map<String, List<String>> bucket =
                d.getKind() == DependencyKind.REQUIRES ? requires : implies;
            bucket.computeIfAbsent(src, k -> new ArrayList<>()).add(dst);
        }

        // orden estable para que el front no perciba cambios espurios
        requires.values().forEach(java.util.Collections::sort);
        implies.values().forEach(java.util.Collections::sort);

        return new PermissionDependenciesGraphDTO(requires, implies);
    }

    @Transactional
    public PermissionDependencyDTO create(@NonNull PermissionDependencyDTO dto) {
        if (Objects.equals(dto.permissionCode(), dto.dependsOnCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Un permiso no puede depender de sí mismo");
        }

        Permission src = permRepo.findByCode(dto.permissionCode())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Permiso no encontrado: " + dto.permissionCode()));
        Permission dst = permRepo.findByCode(dto.dependsOnCode())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Permiso no encontrado: " + dto.dependsOnCode()));

        depRepo.findByPermissionCodeAndDependsOnCodeAndKind(
                dto.permissionCode(), dto.dependsOnCode(), dto.kind())
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La dependencia ya existe");
            });

        PermissionDependency d = new PermissionDependency();
        d.setPermission(src);
        d.setDependsOn(dst);
        d.setKind(dto.kind());
        depRepo.save(d);
        return dto;
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!depRepo.existsById(id)) {
            throw new ResourceNotFoundException("Dependencia no encontrada: " + id);
        }
        depRepo.deleteById(id);
    }
}
