package com.lendora.users.entity;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lendora.users.enums.UserStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // USER FIELDS
  @Column(unique = true, nullable = false, length = 120)
  private String username;

  @Column(nullable = false, length = 120)
  private String password; // BCrypt

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private UserStatus status; // ACTIVE / INACTIVE

  @Transient
  private String token; // NO se persiste

  @Column(length = 64)
  private String office;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "user_accessible_offices",
      joinColumns = @JoinColumn(name = "user_id")
  )
  @Column(name = "office", length = 64, nullable = false)
  private List<String> accessibleOffices = List.of();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id")
  )
  @Column(name = "role", length = 64, nullable = false)
  private Set<String> roles = new HashSet<>(); // e.g. ADMIN, CREDIT_APPROVER

  // PERSONAL
  @Column(length = 80)
  private String firstname;

  @Column(length = 80)
  private String lastname;

  @Column(length = 120)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(name = "last_login_at")
  private OffsetDateTime lastLoginAt;
}