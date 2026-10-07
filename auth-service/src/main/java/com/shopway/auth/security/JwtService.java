package com.shopway.auth.security;

import com.shopway.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        this.expiration = expiration;
    }

    public String generateToken(
            User user
    ) {

        Date now =
                new Date();

        Date expiry =
                new Date(
                        now.getTime() +
                                expiration
                );

        return Jwts.builder()
                .subject(
                        user.getEmail()
                )
                .claim(
                        "userId",
                        user.getId()
                )
                .claim(
                        "firstName",
                        user.getFirstName()
                )
                .claim(
                        "lastName",
                        user.getLastName()
                )
                .claim(
                        "role",
                        user.getRole().name()
                )
                .issuedAt(now)
                .expiration(expiry)
                .signWith(
                        secretKey,
                        Jwts.SIG.HS384
                )
                .compact();
    }

    public String extractUsername(
            String token
    ) {

        return getClaims(token)
                .getSubject();
    }

    public Long extractUserId(
            String token
    ) {

        return getClaims(token)
                .get(
                        "userId",
                        Long.class
                );
    }

    public String extractRole(
            String token
    ) {

        return getClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public boolean isTokenValid(
            String token
    ) {

        try {

            Claims claims =
                    getClaims(token);

            return claims
                    .getExpiration()
                    .after(new Date());

        } catch (Exception exception) {

            return false;
        }
    }

    private Claims getClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

