package com.lendora.users.config;


import java.util.ArrayList;
import java.util.Collection;

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
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.core.convert.converter.Converter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${lendora.security.jwt.issuer}")
    private String issuer;

    @Value("${lendora.security.jwt.expected-audience}")
    private String expectedAudience;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwksUri;  
    
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
           .cors(Customizer.withDefaults())
           .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/users/auth/**").hasAuthority("SCOPE_users.read_auth")
                .anyRequest().authenticated()
           )
           .oauth2ResourceServer(oauth -> oauth
               .jwt(jwt -> {
                     jwt.decoder(jwtDecoder());
                     jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
               })
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
            // 1) Authorities de aplicación (roles)
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Object rawAuth = jwt.getClaims().get("authorities");
            if (rawAuth instanceof Collection<?> col) {
                for (Object o : col) {
                    if (o != null) {
                        authorities.add(new SimpleGrantedAuthority(o.toString()));
                    }
                }
            }

            // 2) Scopes M2M → SCOPE_*
            authorities.addAll(scopesConverter.convert(jwt));

            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

}