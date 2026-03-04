package com.example.com.posts_rest_api.controller;

import com.example.com.posts_rest_api.dto.ApiResponse;
import com.example.com.posts_rest_api.dto.AuthDtos.LoginDto;
import com.example.com.posts_rest_api.dto.AuthDtos.MeResponse;
import com.example.com.posts_rest_api.dto.AuthDtos.UserRegisterDto;
import com.example.com.posts_rest_api.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final String cookieName;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.cookieName:access_token}") String cookieName
    ) {
        this.authService = authService;
        this.cookieName = cookieName;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody UserRegisterDto dto,
            HttpServletResponse response
    ) {
        String jwt = authService.register(dto);
        issueAccessCookie(response, jwt);
        return ResponseEntity.ok(new ApiResponse("Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginDto dto,
            HttpServletResponse response
    ) {
        String jwt = authService.login(dto);
        issueAccessCookie(response, jwt);
        return ResponseEntity.ok(new ApiResponse("Signed in successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        response.addHeader(
                "Set-Cookie",
                cookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );
        return ResponseEntity.ok(new ApiResponse("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return ResponseEntity.status(401).body(new ApiResponse("Not authenticated"));
        return ResponseEntity.ok(new MeResponse(principal.getUsername()));
    }

    private void issueAccessCookie(HttpServletResponse response, String jwt) {
        response.addHeader(
                "Set-Cookie",
                cookieName + "=" + jwt + "; Path=/; HttpOnly; SameSite=Lax"
        );
        // For production HTTPS add: "; Secure"
    }
}