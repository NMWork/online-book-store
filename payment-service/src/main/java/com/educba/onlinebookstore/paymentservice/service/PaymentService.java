package com.educba.onlinebookstore.paymentservice.service;

import com.educba.onlinebookstore.paymentservice.dto.PaymentRequest;
import com.educba.onlinebookstore.paymentservice.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request, String tokenValue);
    PaymentResponse getPaymentById(Long id);
    PaymentResponse getPaymentByOrderId(Long orderId);

}
