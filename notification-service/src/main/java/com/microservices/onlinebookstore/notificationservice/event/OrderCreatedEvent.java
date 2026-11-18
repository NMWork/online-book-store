package com.microservices.onlinebookstore.notificationservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(Long orderId,
                                Long customerId,
                                String status,
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
