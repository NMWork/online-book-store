package com.educba.onlinebookstore.authservice.controller;

import com.educba.onlinebookstore.authservice.dto.AuthResponse;
import com.educba.onlinebookstore.authservice.dto.LoginRequest;
import com.educba.onlinebookstore.authservice.dto.RegisterRequest;
import com.educba.onlinebookstore.authservice.entity.User;
import com.educba.onlinebookstore.authservice.security.JwtUtil;
import com.educba.onlinebookstore.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = authService.register(registerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(buildAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = authService.authenticate(loginRequest);
        return ResponseEntity.ok(buildAuthResponse(user));
    }


    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole(), jwtExpirationMs);
    }

}


