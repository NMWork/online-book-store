package com.microservices.onlinebookstore.orderservice.service;

import com.microservices.onlinebookstore.orderservice.constants.OrderStatus;
import com.microservices.onlinebookstore.orderservice.dto.OrderRequest;
import com.microservices.onlinebookstore.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request, Long userId);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    void deleteOrder(Long id);

    List<OrderResponse> getAllOrdersByStatus(OrderStatus status);
}
