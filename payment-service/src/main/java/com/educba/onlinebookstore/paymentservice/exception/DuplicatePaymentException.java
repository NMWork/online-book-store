package com.educba.onlinebookstore.paymentservice.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(Long orderId) {
        super("A payment already exists for order id " + orderId);
    }
}
