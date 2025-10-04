package com.lendora.users.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lendora.users.dto.MenuItemDTO;
import com.lendora.users.service.MenuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

  @GetMapping
  public List<MenuItemDTO> getMyMenu(Authentication auth) {
    // authorities de tu JWT: típicamente "users.view", "roles.manage", etc.
    Set<String> authorities = auth.getAuthorities()
        .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

    return menuService.getMenuForAuthorities(authorities);
  }
}