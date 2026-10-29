package com.microservices.onlinebookstore.orchestratorservice.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(Long orderId) {
        super("Payment already exists for order " + orderId);
    }
}
