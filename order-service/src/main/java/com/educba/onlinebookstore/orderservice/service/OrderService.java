package com.educba.onlinebookstore.orderservice.service;

import com.educba.onlinebookstore.orderservice.constants.OrderStatus;
import com.educba.onlinebookstore.orderservice.dto.OrderRequest;
import com.educba.onlinebookstore.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    void deleteOrder(Long id);

}
