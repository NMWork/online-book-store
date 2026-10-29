package com.microservices.onlinebookstore.orchestratorservice.dto;


import com.microservices.onlinebookstore.orchestratorservice.constants.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId
) {
}
