package com.microservices.onlinebookstore.paymentservice.service;

import com.microservices.onlinebookstore.paymentservice.dto.PaymentRequest;
import com.microservices.onlinebookstore.paymentservice.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPaymentById(Long id);
    PaymentResponse getPaymentByOrderId(Long orderId);
    PaymentResponse reconcilePayment(Long orderId);

}
