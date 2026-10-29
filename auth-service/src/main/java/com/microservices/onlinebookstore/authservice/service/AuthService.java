package com.microservices.onlinebookstore.authservice.service;

import com.microservices.onlinebookstore.authservice.dto.LoginRequest;
import com.microservices.onlinebookstore.authservice.dto.RegisterRequest;
import com.microservices.onlinebookstore.authservice.entity.User;

public interface AuthService {
    User register(RegisterRequest request);
    User authenticate(LoginRequest request);
}
