package com.example.com.posts_rest_api.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessSeconds;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.accessSeconds:900}") long accessSeconds,
            @Value("${app.jwt.issuer:posts-rest-api}") String issuer
    ) {
        // Keys.hmacShaKeyFor() Creates a new SecretKey instance for use with HMAC-SHA algorithms based on the specified key byte array.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.issuer = issuer;
    }

    /** Create a short-lived access token */
    public String generateAccessToken(String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(email)               // subject = user identity
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)                // HS256 auto-chosen based on key type
                .compact();
    }

    /** Validate token signature/expiry and return subject (email). Throws JwtException if invalid. */
    public String validateAndGetSubject(String jwt) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)        // optional but nice
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }
}