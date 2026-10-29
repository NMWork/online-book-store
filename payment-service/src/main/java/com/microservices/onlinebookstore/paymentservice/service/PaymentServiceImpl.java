package com.microservices.onlinebookstore.paymentservice.service;

import com.microservices.onlinebookstore.paymentservice.constants.PaymentStatus;
import com.microservices.onlinebookstore.paymentservice.dto.PaymentRequest;
import com.microservices.onlinebookstore.paymentservice.dto.PaymentResponse;
import com.microservices.onlinebookstore.paymentservice.entity.Payment;
import com.microservices.onlinebookstore.paymentservice.exception.DuplicatePaymentException;
import com.microservices.onlinebookstore.paymentservice.exception.PaymentNotFoundException;
import com.microservices.onlinebookstore.paymentservice.gateway.MockPaymentGateway;
import com.microservices.onlinebookstore.paymentservice.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.Optional;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_ORDER_UNIQUE_CONSTRAINT = "uk_payment_order_id";
    private static final String LEGACY_PAYMENT_ORDER_UNIQUE_CONSTRAINT = "uk8vo36cen604as7etdfwmyjsxt";

    private final PaymentRepository paymentRepository;
    private final MockPaymentGateway mockPaymentGateway;
    private final PaymentDuplicateResolver duplicateResolver;
    private final TransactionTemplate transactionTemplate;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            MockPaymentGateway mockPaymentGateway,
            PaymentDuplicateResolver duplicateResolver,
            PlatformTransactionManager transactionManager) {
        this.paymentRepository = paymentRepository;
        this.mockPaymentGateway = mockPaymentGateway;
        this.duplicateResolver = duplicateResolver;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {

        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.orderId());
        if (existingPayment.isPresent()) {
            return toResponse(existingPayment.get());
        }

        try {
            return transactionTemplate.execute(status -> createPaymentInTransaction(request));
        } catch (DuplicatePaymentException ex) {
            // TransactionTemplate has rolled back and released its connection before this
            // separate Spring bean starts a fresh read transaction.
            return duplicateResolver.findExistingPayment(request.orderId());
        }
    }

    private PaymentResponse createPaymentInTransaction(PaymentRequest request) {
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        try {
            // Flush here so a unique-key race is detected before gateway processing starts.
            payment = paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException ex) {
            if (!isDuplicateKeyViolation(ex)) {
                throw ex;
            }

            log.warn("Concurrent payment insert detected for order {}", request.orderId());
            throw new DuplicatePaymentException(request.orderId());
        }

        MockPaymentGateway.MockGatewayResult result =
                mockPaymentGateway.process(request.orderId(), request.paymentMethod());

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

    @Override
    @Transactional
    public PaymentResponse reconcilePayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for order id " + orderId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return toResponse(payment);
        }

        MockPaymentGateway.MockGatewayResult result =
                mockPaymentGateway.getResult(payment.getOrderId(), payment.getPaymentMethod());
        payment.setStatus(result.status());
        payment.setTransactionId(result.transactionId());

        return toResponse(paymentRepository.save(payment));
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

    private boolean isDuplicateKeyViolation(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException constraintViolation) {
                return isPaymentOrderConstraint(constraintViolation.getConstraintName());
            }
            if (current instanceof SQLException sqlException) {
                boolean duplicateKey = sqlException.getErrorCode() == 1062
                        || "23505".equals(sqlException.getSQLState());
                String message = sqlException.getMessage();
                if (duplicateKey && message != null) {
                    String normalizedMessage = message.toLowerCase();
                    if (normalizedMessage.contains(PAYMENT_ORDER_UNIQUE_CONSTRAINT)
                            || normalizedMessage.contains(LEGACY_PAYMENT_ORDER_UNIQUE_CONSTRAINT)) {
                        return true;
                    }
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isPaymentOrderConstraint(String constraintName) {
        return constraintName != null
                && (PAYMENT_ORDER_UNIQUE_CONSTRAINT.equalsIgnoreCase(constraintName)
                || LEGACY_PAYMENT_ORDER_UNIQUE_CONSTRAINT.equalsIgnoreCase(constraintName));
    }

}
