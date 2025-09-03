package com.lendora.users.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth
            // preflight del navegador (por si acaso)
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // actuator abiertos
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            // 🔓 endpoint interno para Auth -> Users (sin Bearer):
            .requestMatchers(HttpMethod.GET, "/users/auth/**").permitAll()
            // 🔒 todo lo demás requiere JWT válido (RS256 vía JWKS)
            .anyRequest().authenticated()
        )
        // valida "Authorization: Bearer ..." contra tu JWKS
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())); // ← valida RS256 via JWKS
        return http.build();
    }
}