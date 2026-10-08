package com.shopway.gateway.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // Disable CSRF because this is a stateless REST API
                .csrf(csrf -> csrf.disable())

                // Enable CORS handling
                .cors(cors -> {})

                // JWT-based authentication is stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight requests
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health",
                                "/api/catalog/**",
                                "/api/cart/guest",
                                "/api/cart/guest/**"
                        ).permitAll()

                        // Admin endpoints
                        .requestMatchers(
                                "/api/admin/**"
                        )
                        .hasRole("ADMIN")

                        // User endpoints
                        .requestMatchers(
                                "/api/cart/**",
                                "/api/orders/**"
                        )
                        .hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // Catalog endpoints
//                        .requestMatchers(
//                                "/api/catalog/**"
//                        )
                        // Everything else
                        .anyRequest()
                        .authenticated()
                )

                // Validate access JWT
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(jwt ->
                                        jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter()
                                        )
                                )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey key = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA384"
        );

        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        /*
         * Our JWT contains:
         *
         * "role": "USER"
         *
         * Convert:
         *
         * USER  -> ROLE_USER
         * ADMIN -> ROLE_ADMIN
         */
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }
}

