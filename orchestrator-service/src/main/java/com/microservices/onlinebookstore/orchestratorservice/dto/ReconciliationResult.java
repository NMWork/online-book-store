package com.microservices.onlinebookstore.orchestratorservice.dto;

public enum ReconciliationResult {
    COMPLETED,
    STILL_PENDING,
    PAYMENT_NOT_FOUND,
    SERVICE_UNAVAILABLE,
    INVALID_SAGA_STATE,
    CONCURRENT_UPDATE
}
