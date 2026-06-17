package com.lendora.users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lendora.users.entity.PermissionDependency;
import com.lendora.users.enums.DependencyKind;

@Repository
public interface PermissionDependencyRepository extends JpaRepository<PermissionDependency, Long> {

    /**
     * Trae todo el grafo con permisos resueltos para serializar a Map.
     * fetch join: 1 query, sin N+1.
     */
    @Query("""
        select pd from PermissionDependency pd
            join fetch pd.permission
            join fetch pd.dependsOn
    """)
    List<PermissionDependency> findAllWithRefs();

    Optional<PermissionDependency> findByPermissionCodeAndDependsOnCodeAndKind(
        String permissionCode, String dependsOnCode, DependencyKind kind);
}
