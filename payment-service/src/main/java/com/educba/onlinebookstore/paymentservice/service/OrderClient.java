package com.educba.onlinebookstore.paymentservice.service;

import com.educba.onlinebookstore.paymentservice.exception.OrderNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class OrderClient {

    private final RestClient restClient;
    public OrderClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://order-service").build();
    }

    public void verifyOrderExists(Long orderId, String tokenValue) {
        try {
            restClient.get()
                    .uri("/api/orders/{id}", orderId)
                    .header("authorization", "Bearer "+tokenValue)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new OrderNotFoundException(orderId);
        }
    }


}
