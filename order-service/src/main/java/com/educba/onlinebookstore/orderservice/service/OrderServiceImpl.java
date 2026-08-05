package com.educba.onlinebookstore.orderservice.service;

import com.educba.onlinebookstore.orderservice.constants.OrderStatus;
import com.educba.onlinebookstore.orderservice.dto.OrderItemRequest;
import com.educba.onlinebookstore.orderservice.dto.OrderItemResponse;
import com.educba.onlinebookstore.orderservice.dto.OrderRequest;
import com.educba.onlinebookstore.orderservice.dto.OrderResponse;
import com.educba.onlinebookstore.orderservice.entity.Order;
import com.educba.onlinebookstore.orderservice.entity.OrderItem;
import com.educba.onlinebookstore.orderservice.exception.OrderNotFoundException;
import com.educba.onlinebookstore.orderservice.exception.ProductNotFoundException;
import com.educba.onlinebookstore.orderservice.repository.OrderRepository;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Order order = prepareOrder(request, OrderStatus.CREATED);
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

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(status);
        return prepareOrderResponse(order);
    }

    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        orderRepository.delete(order);
    }

    private Order prepareOrder(OrderRequest request, OrderStatus status) {
        List<OrderItem> orderItems = prepareOrderItems(request.items());
        Order order =  Order.builder()
                .customerId(request.customerId())
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
        if(!PRODUCT_PRICES.containsKey(productId)) {
            throw new ProductNotFoundException(productId);
        }
        BigDecimal unitPrice =
                PRODUCT_PRICES.get(productId);

        if (unitPrice == null) {
            throw new IllegalArgumentException(
                    "Invalid Product Id");
        }

        return unitPrice;
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

    private static final Map<Long, BigDecimal> PRODUCT_PRICES =
            Map.of(
                    1L, BigDecimal.valueOf(799),
                    2L, BigDecimal.valueOf(999),
                    3L, BigDecimal.valueOf(499)
            );
}
