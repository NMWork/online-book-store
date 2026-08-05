package com.educba.onlinebookstore.orderservice.repository;

import com.educba.onlinebookstore.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
