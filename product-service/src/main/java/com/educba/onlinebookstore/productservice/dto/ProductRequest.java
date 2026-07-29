package com.educba.onlinebookstore.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String isbn,
        String description,
        String category,
        @NotNull @Positive BigDecimal price,
        @NotNull @PositiveOrZero Integer stock
) {
}
