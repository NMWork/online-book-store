package com.educba.onlinebookstore.paymentservice.service;

import com.educba.onlinebookstore.paymentservice.dto.PaymentResponse;
import com.educba.onlinebookstore.paymentservice.entity.Payment;
import com.educba.onlinebookstore.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentDuplicateResolver {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse findExistingPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException(
                        "Duplicate payment detected, but no payment was found for order " + orderId));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionId()
        );
    }
}
