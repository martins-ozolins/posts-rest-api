package com.example.com.posts_rest_api.dto;

import com.example.com.posts_rest_api.model.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class AdminDtos {

    public record UserAdminResponse(UUID id, String email, String name, Instant createdAt, Set<Role> roles) {}

    public record UpdateUserRequest(String name, Set<Role> roles) {}
}
