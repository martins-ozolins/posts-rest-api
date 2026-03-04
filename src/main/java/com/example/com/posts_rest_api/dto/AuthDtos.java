package com.example.com.posts_rest_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
}