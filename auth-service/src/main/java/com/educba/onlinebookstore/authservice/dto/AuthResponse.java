package com.educba.onlinebookstore.authservice.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        String username,
        String role,
        Long expiresIn
) {
}
