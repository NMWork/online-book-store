package com.microservices.onlinebookstore.orderservice.service;

import com.microservices.onlinebookstore.orderservice.dto.ProductResponse;
import com.microservices.onlinebookstore.orderservice.exception.ProductNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://product-service").build();
    }

    public ProductResponse getProduct(Long id) {
        return restClient.get()
                .uri("/api/products/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ProductNotFoundException(id);
                })
                .body(ProductResponse.class);
    }

}
