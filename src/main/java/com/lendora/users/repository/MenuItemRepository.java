package com.lendora.users.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lendora.users.entity.MenuItem;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Query("""
      select distinct m
      from MenuItem m
      left join fetch m.children c
      where m.parent is null
      order by m.displayOrder asc nulls last, m.id asc
    """)
  List<MenuItem> findRootWithChildren();
}