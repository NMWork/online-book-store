package com.educba.onlinebookstore.orderservice.repository;

import com.educba.onlinebookstore.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
