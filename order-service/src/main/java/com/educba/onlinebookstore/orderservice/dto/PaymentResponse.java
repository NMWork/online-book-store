package com.educba.onlinebookstore.orderservice.dto;

import com.educba.onlinebookstore.orderservice.constants.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId
) {
}
