package com.lendora.users.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lendora.users.entity.MenuItemPermission;

@Repository
public interface MenuItemPermissionRepository extends JpaRepository<MenuItemPermission, Long>{
  @Query("""
      select mip
      from MenuItemPermission mip
      join fetch mip.menuItem mi
      join fetch mip.permission p
    """)
  List<MenuItemPermission> findAllWithRefs();
}