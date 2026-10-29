package com.educba.onlinebookstore.orchestratorservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class ServiceTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken() {

        Instant now = Instant.now();
        Instant expiry = now.plus(5, ChronoUnit.MINUTES);

        SecretKey key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        return Jwts.builder()
                .subject("orchestrator-service")
                .claim("role", "INTERNAL_SERVICE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }
}

