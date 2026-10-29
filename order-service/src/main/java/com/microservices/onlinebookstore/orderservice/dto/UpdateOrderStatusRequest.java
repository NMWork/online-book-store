package com.microservices.onlinebookstore.orderservice.dto;

import com.microservices.onlinebookstore.orderservice.constants.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
