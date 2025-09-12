package com.lendora.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lendora.users.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByCodeIgnoreCase(String code);
    Page<Permission> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String code, String desc, Pageable pageable);
}