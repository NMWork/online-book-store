package com.educba.onlinebookstore.orderservice.repository;

import com.educba.onlinebookstore.orderservice.constants.OrderStatus;
import com.educba.onlinebookstore.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}
