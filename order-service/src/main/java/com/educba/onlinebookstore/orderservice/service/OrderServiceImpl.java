package com.educba.onlinebookstore.orderservice.service;

import com.educba.onlinebookstore.orderservice.constants.OrderStatus;
import com.educba.onlinebookstore.orderservice.dto.*;
import com.educba.onlinebookstore.orderservice.entity.Order;
import com.educba.onlinebookstore.orderservice.entity.OrderItem;
import com.educba.onlinebookstore.orderservice.exception.OrderNotFoundException;
import com.educba.onlinebookstore.orderservice.exception.ProductNotFoundException;
import com.educba.onlinebookstore.orderservice.repository.OrderRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderServiceImpl(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        Order order = prepareOrder(request, OrderStatus.CREATED, userId);
        Order savedOrder = orderRepository.save(order);
        return prepareOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return prepareOrderResponse(order);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this:: prepareOrderResponse)
                .toList();
    }

    @Transactional
    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return prepareOrderResponse(updatedOrder);
    }

    @Transactional
    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        orderRepository.delete(order);
    }

    @Override
    public List<OrderResponse> getAllOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this :: prepareOrderResponse)
                .toList();
    }

    private Order prepareOrder(OrderRequest request, OrderStatus status, Long customerId) {
        List<OrderItem> orderItems = prepareOrderItems(request.items());
        Order order =  Order.builder()
                .customerId(customerId)
                .status(status)
                .totalAmount(prepareTotalAmount(orderItems))
                .build();

        orderItems.forEach(order::addItem);

        return order;
    }

    private BigDecimal prepareTotalAmount(List<OrderItem> orderItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem orderItem : orderItems) {
            BigDecimal tempTotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            totalAmount = totalAmount.add(tempTotal);
        }
        return totalAmount;
    }

    private List<OrderItem> prepareOrderItems(@NotEmpty List<OrderItemRequest> items) {
        return items.stream()
                .map(request -> OrderItem.builder()
                        .productId(request.productId())
                        .quantity(request.quantity())
                        .unitPrice(prepareUnitPrice(request.productId()))
                        .build())
                .toList();
    }

    private BigDecimal prepareUnitPrice(Long productId) {
        ProductResponse productResponse = productClient.getProduct(productId);
        if (productResponse == null) {
            throw new ProductNotFoundException(productId);
        }
        return productResponse.price();
    }

    private OrderResponse prepareOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(
                        order.getItems()
                                .stream()
                                .map(this::prepareOrderItemResponse)
                                .toList()
                )
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse prepareOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }

}
