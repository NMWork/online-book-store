package com.educba.onlinebookstore.paymentservice.dto;

import com.educba.onlinebookstore.paymentservice.constants.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId
) {
}
