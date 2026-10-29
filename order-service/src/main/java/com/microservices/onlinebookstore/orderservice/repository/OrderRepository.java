package com.microservices.onlinebookstore.orderservice.repository;

import com.microservices.onlinebookstore.orderservice.constants.OrderStatus;
import com.microservices.onlinebookstore.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}
