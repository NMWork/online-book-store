package com.educba.onlinebookstore.authservice.service;

import com.educba.onlinebookstore.authservice.dto.LoginRequest;
import com.educba.onlinebookstore.authservice.dto.RegisterRequest;
import com.educba.onlinebookstore.authservice.entity.User;
import com.educba.onlinebookstore.authservice.exception.InvalidCredentialsException;
import com.educba.onlinebookstore.authservice.exception.UserAlreadyExistsException;
import com.educba.onlinebookstore.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User register(RegisterRequest request) {
        if(userRepository.existsByUsername(request.username())){
            throw new UserAlreadyExistsException("Username "+request.username()+" already exists");
        }

        if(userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException("Email "+request.email()+" already exists");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role("CUSTOMER")
                .build();

        userRepository.save(user);
        return user;
    }

    @Override
    public User authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid UserName or Password"
                ));
        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new  InvalidCredentialsException("Invalid UserName or Password");
        }

        return user;
    }
}
