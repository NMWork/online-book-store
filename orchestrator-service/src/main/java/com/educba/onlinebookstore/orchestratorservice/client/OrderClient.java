package com.educba.onlinebookstore.orchestratorservice.client;

import com.educba.onlinebookstore.orchestratorservice.dto.OrderRequest;
import com.educba.onlinebookstore.orchestratorservice.dto.OrderResponse;
import com.educba.onlinebookstore.orchestratorservice.dto.UpdateOrderStatusRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://order-service").build();
    }

    public OrderResponse createOrder(OrderRequest orderRequest, String token) {
        return restClient.post()
                .uri("/api/orders")
                .header("Authorization", "Bearer " + token)
                .body(orderRequest)
                .retrieve()
                .body(OrderResponse.class);
    }

    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest updateOrderStatusRequest,
                                           String token) {
        return restClient.patch()
                .uri("/api/orders/{id}/status", id)
                .header("Authorization", "Bearer "+token)
                .body(updateOrderStatusRequest)
                .retrieve()
                .body(OrderResponse.class);
    }

}
