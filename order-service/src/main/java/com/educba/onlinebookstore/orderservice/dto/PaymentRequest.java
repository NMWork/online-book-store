package com.educba.onlinebookstore.orderservice.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        Long orderId,
        BigDecimal amount,
        String paymentMethod
) {
}
