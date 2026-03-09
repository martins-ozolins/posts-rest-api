package com.example.com.posts_rest_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    public record UserRegisterDto(
            @Email @NotBlank String email,
            @NotBlank String name,
            @NotBlank String password
    ) {}

    public record LoginDto(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record MeResponse(String email) {}

    public record JwtClaims(String email, UUID userId, UUID jti){}

    public record TokenPair(String accessToken, String refreshToken) {}

    public record RefreshTokenResult(String token, Instant expiresAt) {}

}