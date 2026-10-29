package com.microservices.onlinebookstore.orchestratorservice.constants;

public enum SagaStatus {
    STARTED,
    PAYMENT_PENDING,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_UNKNOWN,
    COMPLETED,
    PAYMENT_MANUAL_REVIEW
}
