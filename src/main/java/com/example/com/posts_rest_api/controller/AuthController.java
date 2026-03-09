package com.example.com.posts_rest_api.controller;

import com.example.com.posts_rest_api.dto.ApiResponse;
import com.example.com.posts_rest_api.dto.AuthDtos.*;
import com.example.com.posts_rest_api.dto.AuthDtos.LoginDto;
import com.example.com.posts_rest_api.dto.AuthDtos.MeResponse;
import com.example.com.posts_rest_api.dto.AuthDtos.UserRegisterDto;
import com.example.com.posts_rest_api.exceptions.InvalidCredentialsException;
import com.example.com.posts_rest_api.model.RefreshToken;
import com.example.com.posts_rest_api.security.JwtService;
import com.example.com.posts_rest_api.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final String accessTokenName;
    private final String refreshTokenName;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.cookieName:access_token}") String accessTokenName,
            @Value("${app.jwt.refreshCookieName:refresh_token}") String refreshTokenName
    ) {
        this.authService = authService;
        this.accessTokenName = accessTokenName;
        this.refreshTokenName = refreshTokenName;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody UserRegisterDto dto,
            HttpServletResponse response
    ) {
        authService.register(dto);

        return ResponseEntity.ok(new ApiResponse("Registered successfully. Please sign in."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginDto dto,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = authService.login(dto);
        issueCookie(response, tokenPair.accessToken(), "access_token");
        issueCookie(response, tokenPair.refreshToken(), "refresh_token");

        return ResponseEntity.ok(new ApiResponse("Signed in successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String token = Arrays.stream(cookies != null ? cookies : new Cookie[0])
                .filter(cookie -> refreshTokenName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(InvalidCredentialsException::new);


        authService.logout(token);

        response.addHeader(
                "Set-Cookie",
                accessTokenName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );
        response.addHeader(
                "Set-Cookie",
                refreshTokenName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );
        return ResponseEntity.ok(new ApiResponse("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return ResponseEntity.status(401).body(new ApiResponse("Not authenticated"));
        return ResponseEntity.ok(new MeResponse(principal.getUsername()));
    }

   @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        String token = Arrays.stream(cookies != null ? cookies : new Cookie[0])
                .filter(cookie -> refreshTokenName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(InvalidCredentialsException::new);


        TokenPair tokenPair = authService.refresh(token);


       issueCookie(response, tokenPair.accessToken(), "access_token");
       issueCookie(response, tokenPair.refreshToken(), "refresh_token");

       return ResponseEntity.ok(new ApiResponse("Refreshed successfully"));
   }

    private void issueCookie(HttpServletResponse response, String jwt, String cookieName) {
        response.addHeader(
                "Set-Cookie",
                cookieName + "=" + jwt + "; Path=/; HttpOnly; SameSite=Lax"
        );
        // For production HTTPS add: "; Secure"
    }
}