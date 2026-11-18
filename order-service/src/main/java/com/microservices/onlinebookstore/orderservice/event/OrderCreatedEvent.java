package com.microservices.onlinebookstore.orderservice.event;

import com.microservices.onlinebookstore.orderservice.constants.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(Long orderId,
                                Long customerId,
                                OrderStatus status,
                                BigDecimal totalAmount,
                                LocalDateTime createdAt,
                                List<OrderItemEvent> items) {
    public record OrderItemEvent(
            Long productId,
            Integer quantity,
            BigDecimal unitPrice
    ) {
    }
}
