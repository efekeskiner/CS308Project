package com.bookstore.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
  public class JwtUtils {

    @Value("${app.jwt.secret}")
        private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
        private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
        private long refreshExpirationMs;

    private Key getSigningKey() {
              return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(Long userId, String email, String role) {
              return Jwts.builder()
                                .setSubject(email)
                                .claim("userId", userId)
                                .claim("role", role)
                                .setIssuedAt(new Date())
                                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                                .compact();
    }

    public String generateRefreshToken(String email) {
              return Jwts.builder()
                                .setSubject(email)
                                .setIssuedAt(new Date())
                                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                                .compact();
    }

    public boolean validateToken(String token) {
              try {
                            Jwts.parserBuilder()
                                              .setSigningKey(getSigningKey())
                                              .build()
                                              .parseClaimsJws(token);
                            return true;
              } catch (JwtException | IllegalArgumentException e) {
                            return false;
              }
    }

    public String getEmailFromToken(String token) {
              return Jwts.parserBuilder()
                                .setSigningKey(getSigningKey())
                                .build()
                                .parseClaimsJws(token)
                                .getBody()
                                .getSubject();
    }

    public String getRoleFromToken(String token) {
              return (String) Jwts.parserBuilder()
                                .setSigningKey(getSigningKey())
                                .build()
                                .parseClaimsJws(token)
                                .getBody()
                                .get("role");
    }
  }
