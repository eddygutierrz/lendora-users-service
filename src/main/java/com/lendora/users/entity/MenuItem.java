package com.lendora.users.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "menu_items")
@Getter @Setter
public class MenuItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 96)
    private String label;

    @Column(length = 192)
    private String route; // ej. /admin/users

    @Column(length = 64)
    private String icon;  // opcional

    @Column(name = "display_order")
    private Integer displayOrder; // null = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MenuItem parent; // null si es raíz

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    @BatchSize(size = 50)
    @Fetch(FetchMode.SUBSELECT)  
    private List<MenuItem> children = new ArrayList<>();

    // Si algún día quieres forzar ALL vs ANY
    @Column(name = "require_all", nullable = false)
    private boolean requireAll = false;
}