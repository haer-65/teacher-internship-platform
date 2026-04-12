package com.teacher.internship.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(Long userId, String loginName, String roleCode) {
        return createToken(userId, loginName, roleCode, jwtProperties.getExpirationSeconds());
    }

    public String createToken(Long userId, String loginName, String roleCode, long expirationSeconds) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("loginName", loginName)
                .claim("roleCode", roleCode)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
