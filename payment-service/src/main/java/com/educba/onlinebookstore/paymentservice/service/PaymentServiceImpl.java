package com.educba.onlinebookstore.paymentservice.service;

import com.educba.onlinebookstore.paymentservice.constants.PaymentStatus;
import com.educba.onlinebookstore.paymentservice.dto.PaymentRequest;
import com.educba.onlinebookstore.paymentservice.dto.PaymentResponse;
import com.educba.onlinebookstore.paymentservice.entity.Payment;
import com.educba.onlinebookstore.paymentservice.exception.DuplicatePaymentException;
import com.educba.onlinebookstore.paymentservice.exception.PaymentNotFoundException;
import com.educba.onlinebookstore.paymentservice.gateway.MockPaymentGateway;
import com.educba.onlinebookstore.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MockPaymentGateway mockPaymentGateway;
    private final OrderClient orderClient;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, String tokenValue) {
        orderClient.verifyOrderExists(request.orderId(), tokenValue);

        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new DuplicatePaymentException(request.orderId());
        }

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        MockPaymentGateway.MockGatewayResult result =
                mockPaymentGateway.process(request.paymentMethod());

        payment.setStatus(result.status());
        payment.setTransactionId(result.transactionId());

        payment = paymentRepository.save(payment);

        return toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for order id " + orderId));
        return toResponse(payment);
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
