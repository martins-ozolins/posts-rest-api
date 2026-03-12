package com.example.com.posts_rest_api.security;

import com.example.com.posts_rest_api.dto.AuthDtos.*;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessSeconds;
    private final long refreshSeconds;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.accessSeconds:900}") long accessSeconds,
            @Value("${app.jwt.refreshSeconds:1296000}") long refreshSeconds,
            @Value("${app.jwt.issuer:posts-rest-api}") String issuer
    ) {
        // Keys.hmacShaKeyFor() Creates a new SecretKey instance for use with HMAC-SHA algorithms based on the specified key byte array.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
        this.issuer = issuer;
    }

    /** Create a short-lived access token */
    public String generateAccessToken(String email, UUID id) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(email)   // subject = user identity
                .claim("userId", id.toString())
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)                // HS256 auto-chosen based on key type
                .compact();
    }

    public RefreshTokenResult generateRefreshToken(String email, UUID userId, UUID jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshSeconds);

        String token = Jwts.builder()
                .issuer(issuer)
                .subject(email)               // subject = user identity
                .claim("userId", userId.toString())
                .claim("jti", jti.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)                // HS256 auto-chosen based on key type
                .compact();

        return new RefreshTokenResult(token, exp);
    }

    /** Validate token signature/expiry and return subject (email). Throws JwtException if invalid. */
    public JwtClaims validateAndGetClaims(String jwt) throws JwtException {
        var payload = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        String email = payload.getSubject();
        UUID userId = UUID.fromString(payload.get("userId", String.class));
        UUID jti = UUID.fromString(payload.get("jti", String.class));


        return new JwtClaims(email, userId, jti);
    }
}