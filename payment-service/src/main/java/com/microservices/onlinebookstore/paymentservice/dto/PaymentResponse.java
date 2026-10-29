package com.microservices.onlinebookstore.paymentservice.dto;

import com.microservices.onlinebookstore.paymentservice.constants.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId
) {
}
