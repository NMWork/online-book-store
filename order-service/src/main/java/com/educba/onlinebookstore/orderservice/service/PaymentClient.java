package com.educba.onlinebookstore.orderservice.service;

import com.educba.onlinebookstore.orderservice.dto.PaymentRequest;
import com.educba.onlinebookstore.orderservice.dto.PaymentResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://payment-service").build();
    }

    public PaymentResponse createPayment(PaymentRequest paymentRequest, String token) {
        return restClient.post()
                .uri("/api/payments")
                .header("authorization", "Bearer " + token)
                .body(paymentRequest)
                .retrieve()
                .body(PaymentResponse.class);
    }

}
