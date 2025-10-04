package com.lendora.users.utils;

import com.lendora.users.dto.UpsertUserRequest;
import com.lendora.users.dto.UserAuthDTO;
import com.lendora.users.dto.UserDTO;
import com.lendora.users.entity.User;

public final class UserMapper {
    private UserMapper(){}

    public static UserAuthDTO toAuthDTO(com.lendora.users.entity.User u) {
    if (u == null) return null;
    return new UserAuthDTO(
        u.getId(),
        u.getUsername(),
        u.getPassword(),
        u.getRoles(),
        u.getStatus()
    );
  }

  public static UserDTO toDTO(com.lendora.users.entity.User u) {
    if (u == null) return null;
    return new UserDTO(
        u.getId(),
        u.getUsername(),
        u.getFirstname(),
        u.getLastname(),
        u.getEmail(),
        u.getPhone(),
        u.getOffice(),
        u.getAccessibleOffices(),
        u.getRoles(),
        u.getStatus(),
        u.getLastLoginAt()
    );
  }

  public static void apply(User u, UpsertUserRequest r, java.util.function.Function<String,String> encoderIfPresent) {
        u.setUsername(r.username());
        if (r.password() != null && !r.password().isBlank()) {
            // codifica si viene
            u.setPassword(encoderIfPresent.apply(r.password()));
        }
        u.setFirstname(r.firstname());
        u.setLastname(r.lastname());
        u.setEmail(r.email());
        u.setPhone(r.phone());
        u.setOffice(r.office());
        u.setAccessibleOffices(r.accessibleOffices());
        u.setRoles(r.roles());
        u.setStatus(r.status());
    }
}