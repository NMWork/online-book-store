package com.educba.onlinebookstore.paymentservice.gateway;

import com.educba.onlinebookstore.paymentservice.constants.PaymentStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Component
public class MockPaymentGateway {

    private static final double SUCCESS_RATE = 0.85;

    public MockGatewayResult process(Long orderId, String paymentMethod) {
        UUID idempotencyId = UUID.nameUUIDFromBytes(
                ("payment-order-" + orderId).getBytes(StandardCharsets.UTF_8));
        boolean success = Math.floorMod(idempotencyId.getLeastSignificantBits(), 100)
                < (int) (SUCCESS_RATE * 100);
        String transactionId = "TXN-" + idempotencyId.toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        PaymentStatus status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        return new MockGatewayResult(status, transactionId);
    }

    public MockGatewayResult getResult(Long orderId, String paymentMethod) {
        // This deterministic mock recreates the same result for the order. A real gateway
        // implementation must look up the original provider operation and never charge here.
        return process(orderId, paymentMethod);
    }

    public record MockGatewayResult(PaymentStatus status, String transactionId) {}

}
