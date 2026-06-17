package com.lendora.users.entity;

import com.lendora.users.enums.DependencyKind;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Dependencia dirigida entre dos permisos.
 *
 * Si en un rol se selecciona `permission`, el front consultará este grafo
 * y, según `kind`, agregará automáticamente `dependsOn` al payload.
 *
 * El unique (permission_id, depends_on_id, kind) evita registros duplicados
 * y el CHECK garantiza que un permiso no se referencie a sí mismo.
 */
@Entity
@Table(
    name = "permission_dependency",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_perm_dep",
            columnNames = {"permission_id", "depends_on_id", "kind"}
        )
    },
    indexes = {
        @Index(name = "idx_perm_dep_perm",       columnList = "permission_id"),
        @Index(name = "idx_perm_dep_depends_on", columnList = "depends_on_id"),
        @Index(name = "idx_perm_dep_kind",       columnList = "kind")
    }
)
@Getter @Setter @NoArgsConstructor
public class PermissionDependency {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depends_on_id")
    private Permission dependsOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DependencyKind kind;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}
