package com.lendora.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lendora.users.entity.Role;
import java.util.List;
import java.util.Set;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByCode(String code);

    List<Role> findByCodeIn(Set<String> codes);

    default Page<Role> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return findAll(pageable);
        // Implementa usando métodos derivados o @Query; aquí derivado simple:
        return findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(q, q, pageable);
    }

    Page<Role> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String c, String n, Pageable pageable);
}