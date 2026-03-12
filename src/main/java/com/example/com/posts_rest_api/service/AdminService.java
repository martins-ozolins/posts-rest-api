package com.example.com.posts_rest_api.service;


import com.example.com.posts_rest_api.dto.AdminDtos.*;
import com.example.com.posts_rest_api.exceptions.NotFoundException;
import com.example.com.posts_rest_api.model.Role;
import com.example.com.posts_rest_api.model.User;
import com.example.com.posts_rest_api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserAdminResponse> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users.stream().map(user -> new UserAdminResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt(), user.getRoles())).toList();
    }

    public UserAdminResponse getUserById(UUID id) {

        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User with " + id + " not found!"));

        return new UserAdminResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt(), user.getRoles());
    }

    public UserAdminResponse updateUserById(UUID id, UpdateUserRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User with " + id + " not found!"));
        if (req.name() != null) user.setName(req.name());
        if (req.roles() != null) user.setRoles(req.roles());
        userRepository.save(user);
        return new UserAdminResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt(), user.getRoles());
    }

    public void deleteUserById(UUID id) {

        User user = userRepository.findById(id).orElseThrow(() ->new  NotFoundException("User with " + id + " not found!"));
        if (user.getRoles().contains(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete an admin user");
        }
        userRepository.delete(user);

    }


}
