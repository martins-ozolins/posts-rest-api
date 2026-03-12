package com.example.com.posts_rest_api.controller;


import com.example.com.posts_rest_api.dto.AdminDtos.*;
import com.example.com.posts_rest_api.model.User;
import com.example.com.posts_rest_api.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

//    GET /admin/users — list all users
//    GET /admin/users/{id} — get user details
//    DELETE /admin/users/{id} — delete/ban a user
//    PATCH /admin/users/{id}/role — change a user's role (e.g. promote to admin

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping
    public List<UserAdminResponse> getAllUsers() {

        return adminService.getAllUsers();

    }

    @GetMapping("/{id}")
    public UserAdminResponse getUser(@PathVariable UUID id) {
        return adminService.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminService.deleteUserById(id);
        return ResponseEntity.noContent().build(); // 204
    }


}
