package com.lendora.users.security;

import com.lendora.audit.context.AuditContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@Order(1) // se ejecuta después del filtro que ya puso el request (orden 0)
public class AuditActorFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
      throws ServletException, IOException {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated()) {
        String actor = String.valueOf(auth.getName()); // o del JWT claim "sub"
        AuditContext.setActor(actor);
      }
    } finally {
      chain.doFilter(request, response);
    }
  }
}
