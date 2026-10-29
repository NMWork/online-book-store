package com.microservices.onlinebookstore.paymentservice.controller;

import com.microservices.onlinebookstore.paymentservice.dto.PaymentRequest;
import com.microservices.onlinebookstore.paymentservice.dto.PaymentResponse;
import com.microservices.onlinebookstore.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    @PostMapping("/{orderId}/reconcile")
    public ResponseEntity<PaymentResponse> reconcilePayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.reconcilePayment(orderId));
    }
}
