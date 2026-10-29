package com.educba.onlinebookstore.orchestratorservice.dto;

import com.educba.onlinebookstore.orchestratorservice.constants.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
