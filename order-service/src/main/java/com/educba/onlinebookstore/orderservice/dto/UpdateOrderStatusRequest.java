package com.educba.onlinebookstore.orderservice.dto;

import com.educba.onlinebookstore.orderservice.constants.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
