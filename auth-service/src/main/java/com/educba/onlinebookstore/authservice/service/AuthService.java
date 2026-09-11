package com.educba.onlinebookstore.authservice.service;

import com.educba.onlinebookstore.authservice.dto.LoginRequest;
import com.educba.onlinebookstore.authservice.dto.RegisterRequest;
import com.educba.onlinebookstore.authservice.entity.User;

public interface AuthService {
    User register(RegisterRequest request);
    User authenticate(LoginRequest request);
}
