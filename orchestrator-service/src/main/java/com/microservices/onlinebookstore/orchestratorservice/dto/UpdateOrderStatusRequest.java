package com.microservices.onlinebookstore.orchestratorservice.dto;

import com.microservices.onlinebookstore.orchestratorservice.constants.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
