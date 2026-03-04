package com.example.com.posts_rest_api.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class PostDtos {

    public record PostCreateRequest(
            @NotBlank String title,
            String subtitle, // nullable by default
            @NotBlank String description
    ) {}

    public record PostUpdateRequest(
            @NotBlank String title,
            String subtitle,
            @NotBlank String description
    ) {}


    public record PostPublicResponse(
            Long id,
            String title,
            String subtitle,
            String description,
            Instant createdAt
    ) {}

    public record PostPrivateResponse(
            Long id,
            String title,
            String subtitle,
            String description,
            Instant createdAt,
            UUID ownerId,
            String ownerEmail
    ) {}
}