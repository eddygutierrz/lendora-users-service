package com.lendora.users.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lendora.users.dto.MenuItemDTO;
import com.lendora.users.entity.MenuItem;
import com.lendora.users.repository.MenuItemPermissionRepository;
import com.lendora.users.repository.MenuItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuItemRepository menuRepo;
    private final MenuItemPermissionRepository mipRepo;

    @Transactional(readOnly = true)
    public List<MenuItemDTO> getMenuForAuthorities(Set<String> authorities) {
        // Mapa: menuItemId -> permisos requeridos (códigos)
        Map<Long, Set<String>> requiredByItem = new HashMap<>();
        for (var mip : mipRepo.findAllWithRefs()) {
        requiredByItem
            .computeIfAbsent(mip.getMenuItem().getId(), k -> new HashSet<>())
            .add(mip.getPermission().getCode());
        }

        // Cargar raíces con hijos (primer nivel). Si tienes subniveles, puedes hacer fetch en cascada.
        List<MenuItem> roots = menuRepo.findRootWithChildren();

        return roots.stream()
        .map(mi -> filter(mi, requiredByItem, authorities))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    private MenuItemDTO filter(MenuItem mi, Map<Long, Set<String>> requiredByItem, Set<String> authorities) {
        boolean visibleSelf = isItemVisible(mi, requiredByItem, authorities);

        // Procesar hijos
        List<MenuItemDTO> visibleChildren = mi.getChildren() == null ? List.of() :
        mi.getChildren().stream()
            .map(child -> filter(child, requiredByItem, authorities))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // Si no tiene permisos propios y no tiene hijos visibles → ocultar
        if (!visibleSelf && visibleChildren.isEmpty()) return null;

        MenuItemDTO dto = new MenuItemDTO(
            mi.getLabel(),
            mi.getRoute(),
            mi.getIcon(),
            visibleChildren
        );
        return dto;
    }

    private boolean isItemVisible(MenuItem mi, Map<Long, Set<String>> requiredByItem, Set<String> authorities) {
        Set<String> req = requiredByItem.getOrDefault(mi.getId(), Set.of());
        if (req.isEmpty()) {
        // Sin requisitos explícitos: visible (útil para grupos)
        return true;
        }
        // ANY por defecto; si quisieras ALL, lee mi.isRequireAll()
        for (String r : req) if (authorities.contains(r)) return true;
        return false;
    }
}