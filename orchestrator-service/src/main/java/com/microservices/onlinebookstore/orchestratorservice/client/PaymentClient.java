package com.microservices.onlinebookstore.orchestratorservice.client;

import com.microservices.onlinebookstore.orchestratorservice.dto.PaymentRequest;
import com.microservices.onlinebookstore.orchestratorservice.dto.PaymentResponse;
import com.microservices.onlinebookstore.orchestratorservice.exception.PaymentNotFoundException;
import com.microservices.onlinebookstore.orchestratorservice.exception.DuplicatePaymentException;
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
                .header("Authorization", "Bearer " + token)
                .body(paymentRequest)
                .retrieve()
                .onStatus(
                        status -> status.value() == 409,
                        (request, response) -> {
                            throw new DuplicatePaymentException(paymentRequest.orderId());
                        }
                )
                .body(PaymentResponse.class);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId, String token) {
        return restClient.get()
                .uri("/api/payments/order/{orderId}", orderId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            throw new PaymentNotFoundException(orderId);
                        }
                )
                .body(PaymentResponse.class);
    }

    public PaymentResponse reconcilePayment(Long orderId, String token) {
        return restClient.post()
                .uri("/api/payments/{orderId}/reconcile", orderId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(PaymentResponse.class);
    }

}
