package com.microservices.onlinebookstore.orchestratorservice.constants;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED,
    PAYMENT_UNKNOWN
}
