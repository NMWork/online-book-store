package com.educba.onlinebookstore.paymentservice.gateway;

import com.educba.onlinebookstore.paymentservice.constants.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockPaymentGateway {

    private static final double SUCCESS_RATE = 0.85;

    public MockGatewayResult process(String paymentMethod) {
        boolean success = ThreadLocalRandom.current().nextDouble() < SUCCESS_RATE;
        String transactionId = "TXN-" + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
        PaymentStatus status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        return new MockGatewayResult(status, transactionId);
    }

    public record MockGatewayResult(PaymentStatus status, String transactionId) {}

}
