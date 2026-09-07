package com.educba.onlinebookstore.orderservice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductResponse(
        Long id,
        String title,
        String author,
        String description,
        String isbn,
        String category,
        BigDecimal price,
        Integer stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
