package com.microservices.onlinebookstore.orderservice.repository;

import com.microservices.onlinebookstore.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
