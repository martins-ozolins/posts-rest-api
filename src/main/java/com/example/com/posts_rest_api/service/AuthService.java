package com.example.com.posts_rest_api.service;

import com.example.com.posts_rest_api.dto.AuthDtos.*;
import com.example.com.posts_rest_api.exceptions.EmailAlreadyUsedException;
import com.example.com.posts_rest_api.exceptions.InvalidCredentialsException;
import com.example.com.posts_rest_api.model.RefreshToken;
import com.example.com.posts_rest_api.model.Role;
import com.example.com.posts_rest_api.model.User;
import com.example.com.posts_rest_api.repository.RefreshTokenRepository;
import com.example.com.posts_rest_api.repository.UserRepository;
import com.example.com.posts_rest_api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository
            ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void register(UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyUsedException();
        }

        User u = new User();
        u.setEmail(dto.email());
        u.setName(dto.name());
        u.setPasswordHash(passwordEncoder.encode(dto.password()));
        u.setRoles(Set.of(Role.USER));

        userRepository.save(u);

    }

    public TokenPair login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId());

        UUID jti = UUID.randomUUID();
        RefreshTokenResult refreshTokenResult = jwtService.generateRefreshToken(user.getEmail(), user.getId(), jti);

        RefreshToken entity = new RefreshToken();
        entity.setId(jti);
        entity.setOwner(user);
        entity.setExpiresAt(refreshTokenResult.expiresAt());
        refreshTokenRepository.save(entity);

        return new TokenPair(accessToken, refreshTokenResult.token());
    }

    public void logout(String token) {
        try {
            JwtClaims claims = jwtService.validateAndGetClaims(token);
            refreshTokenRepository.findById(claims.jti()).ifPresent(t -> {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            });
        } catch (Exception ignored) {
            // token invalid or already expired — nothing to revoke
        }
    }

    public TokenPair refresh(String token) {


        JwtClaims jwtClaims = jwtService.validateAndGetClaims(token);


        RefreshToken refreshTokenData = refreshTokenRepository.findById(jwtClaims.jti())
                .orElseThrow(InvalidCredentialsException::new);

        if (refreshTokenData.getRevoked()) {
            throw new InvalidCredentialsException();
        }

        // revoke old refresh token
        refreshTokenData.setRevoked(true);

        UUID newJti = UUID.randomUUID();
        RefreshTokenResult result = jwtService.generateRefreshToken(jwtClaims.email(), jwtClaims.userId(), newJti);

        RefreshToken newToken = new RefreshToken();
        newToken.setId(newJti);
        newToken.setOwner(userRepository.getReferenceById(jwtClaims.userId()));
        newToken.setExpiresAt(result.expiresAt());
        refreshTokenRepository.save(refreshTokenData); // save revoked old one
        refreshTokenRepository.save(newToken);

        String accessToken = jwtService.generateAccessToken(jwtClaims.email(), jwtClaims.userId());
        return new TokenPair(accessToken, result.token());

    }
}