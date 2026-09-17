package com.educba.onlinebookstore.api_gateway.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try{
            extractAllClaims(token);
            return true;
        }
        catch(ExpiredJwtException e){
            return false;
        }
        catch(JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public String extractUsername(String token){
           return extractAllClaims(token).getSubject();
    }

    public String extractUserId(String token){
        return extractAllClaims(token).get("userId").toString();
    }

    public String extractRole(String token){
        return extractAllClaims(token).get("role").toString();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
