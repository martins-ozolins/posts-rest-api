package com.example.com.posts_rest_api.service;

import com.example.com.posts_rest_api.dto.AuthDtos;
import com.example.com.posts_rest_api.exceptions.EmailAlreadyUsedException;
import com.example.com.posts_rest_api.exceptions.InvalidCredentialsException;
import com.example.com.posts_rest_api.model.Role;
import com.example.com.posts_rest_api.model.User;
import com.example.com.posts_rest_api.repository.UserRepository;
import com.example.com.posts_rest_api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(AuthDtos.UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyUsedException();
        }

        User u = new User();
        u.setEmail(dto.email());
        u.setName(dto.name());
        u.setPasswordHash(passwordEncoder.encode(dto.password()));
        u.setRoles(Set.of(Role.USER));

        userRepository.save(u);

        return jwtService.generateAccessToken(u.getEmail());
    }

    public String login(AuthDtos.LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateAccessToken(user.getEmail());
    }
}