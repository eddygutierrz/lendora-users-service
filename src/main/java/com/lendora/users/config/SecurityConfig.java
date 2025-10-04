package com.lendora.users.config;


import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendora.common.api.ApiError;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    @Value("${lendora.security.jwt.issuer}")
    private String issuer;

    @Value("${lendora.security.jwt.expected-audience}")
    private String expectedAudience;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwksUri;  
    
    @Bean
    SecurityFilterChain filter(HttpSecurity http,
                                AccessDeniedHandler denied,
                                AuthenticationEntryPoint entry) throws Exception {
        http.csrf(csrf -> csrf.disable())
           .cors(Customizer.withDefaults())
           .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // ← clave
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/users/auth/**").hasAuthority("SCOPE_users-service.read_auth")
                .requestMatchers("/users/roles/resolve-permissions").hasAnyAuthority("SCOPE_users-service.read_auth")
                .anyRequest().authenticated()
           )
           .oauth2ResourceServer(oauth -> oauth
               .jwt(jwt -> {
                     jwt.decoder(jwtDecoder());
                     jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
               })
           )
           .exceptionHandling(e -> e
                .accessDeniedHandler(denied)         // 403 JSON
                .authenticationEntryPoint(entry)     // 401 JSON
            );
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();

        // 1) Validaciones por defecto + issuer exacto
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        // 2) Audiencia requerida: debe contener expectedAudience ("users")
        OAuth2TokenValidator<Jwt> withAudience = jwt -> {
            var aud = jwt.getAudience(); // mapea a List<String>
            if (aud != null && aud.contains(expectedAudience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Required audience not present: " + expectedAudience, null)
            );
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        // scopes M2M (space-delimited o array), a SCOPE_*
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setAuthorityPrefix("SCOPE_");
        scopesConverter.setAuthoritiesClaimName("scope"); // también detecta "scp" automáticamente

        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            // Lee SOLO el claim "authorities" (trae ROLE_* y permisos finos)
            Object rawAuth = jwt.getClaims().get("authorities");
            if (rawAuth instanceof Collection<?> col) {
                for (Object o : col) {
                    if (o != null) authorities.add(new SimpleGrantedAuthority(o.toString()));
                }
            }
            // Suma scopes M2M si aplica
            authorities.addAll(scopesConverter.convert(jwt));
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
            "https://admin-portal.impulsofirme.com.mx",
            "http://localhost:4200"  // solo para dev
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(Duration.ofHours(1));
        cfg.setExposedHeaders(List.of("Authorization", "Location", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    AccessDeniedHandler jsonAccessDeniedHandler(ObjectMapper om) {
        return (req, res, ex) -> {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = new ApiError(
            "FORBIDDEN", 403, "Forbidden", "Access denied",
            req.getRequestURI(), OffsetDateTime.now()
        );
        om.writeValue(res.getOutputStream(), body);
        };
    }

    @Bean
    AuthenticationEntryPoint jsonAuthEntryPoint(ObjectMapper om) {
        return (req, res, ex) -> {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = new ApiError(
            "UNAUTHORIZED", 401, "Unauthorized", "Authentication required",
            req.getRequestURI(), OffsetDateTime.now()
        );
        om.writeValue(res.getOutputStream(), body);
        };
    }

}