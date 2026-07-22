package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.auth.LoginRequest;
import com.mzansiconnect.backend.dto.auth.LoginResponse;
import com.mzansiconnect.backend.dto.auth.RegisterRequest;
import com.mzansiconnect.backend.dto.auth.UserResponse;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>>
    register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "User registered successfully",
                                authService.register(request)
                        )
                );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>>
    login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        authService.login(request)
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>>
    getCurrentUser(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Authenticated user retrieved successfully",
                        authService.getCurrentUser(
                                authentication.getName()
                        )
                )
        );
    }
}
