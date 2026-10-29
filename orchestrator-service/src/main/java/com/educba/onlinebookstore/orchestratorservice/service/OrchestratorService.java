package com.educba.onlinebookstore.orchestratorservice.service;

import com.educba.onlinebookstore.orchestratorservice.client.OrderClient;
import com.educba.onlinebookstore.orchestratorservice.client.PaymentClient;
import com.educba.onlinebookstore.orchestratorservice.constants.OrderStatus;
import com.educba.onlinebookstore.orchestratorservice.constants.PaymentStatus;
import com.educba.onlinebookstore.orchestratorservice.constants.SagaStatus;
import com.educba.onlinebookstore.orchestratorservice.dto.*;
import com.educba.onlinebookstore.orchestratorservice.entity.SagaInstance;
import com.educba.onlinebookstore.orchestratorservice.exception.PaymentNotFoundException;
import com.educba.onlinebookstore.orchestratorservice.exception.DuplicatePaymentException;
import com.educba.onlinebookstore.orchestratorservice.repository.SagaInstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class OrchestratorService {

    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final TransactionTemplate transactionTemplate;

    public OrchestratorService(
            OrderClient orderClient,
            PaymentClient paymentClient,
            SagaInstanceRepository sagaInstanceRepository,
            PlatformTransactionManager transactionManager) {
        this.orderClient = orderClient;
        this.paymentClient = paymentClient;
        this.sagaInstanceRepository = sagaInstanceRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    private static final int MAX_RECOVERY_ATTEMPTS = 5;

    public OrderResponse createOrder(OrderRequest orderRequest, String token) {
        OrderResponse orderCreatedResponse = orderClient.createOrder(orderRequest, token);

        SagaInstance sagaInstance = SagaInstance.builder()
                .orderId(orderCreatedResponse.id())
                .paymentMethod(orderRequest.paymentMethod())
                .paymentAmount(orderCreatedResponse.totalAmount())
                .sagaStatus(SagaStatus.STARTED)
                .retryCount(0)
                .build();

        sagaInstanceRepository.save(sagaInstance);

        PaymentRequest paymentRequest = new PaymentRequest(orderCreatedResponse.id(),
                orderCreatedResponse.totalAmount(), orderRequest.paymentMethod());

        sagaInstance.setSagaStatus(SagaStatus.PAYMENT_PENDING);
        sagaInstance = sagaInstanceRepository.save(sagaInstance);

        PaymentResponse paymentResponse;
        try {
            paymentResponse = paymentClient.createPayment(paymentRequest, token);
        } catch (DuplicatePaymentException ex) {
            try {
                paymentResponse = paymentClient.getPaymentByOrderId(orderCreatedResponse.id(), token);
            } catch (Exception lookupException) {
                log.warn("Could not retrieve existing payment for order {} after duplicate response",
                        orderCreatedResponse.id(), lookupException);
                return markPaymentUnknown(orderCreatedResponse.id(), sagaInstance, token);
            }
        } catch (Exception ex) {
            log.error("Payment processing failed/unknown for order {}", orderCreatedResponse.id(), ex);
            return markPaymentUnknown(orderCreatedResponse.id(), sagaInstance, token);
        }

        try {
            paymentResponse = reconcilePendingPayment(orderCreatedResponse.id(), paymentResponse, token);
        } catch (Exception ex) {
            log.warn("Could not reconcile pending payment for order {}", orderCreatedResponse.id(), ex);
            return markPaymentUnknown(orderCreatedResponse.id(), sagaInstance, token);
        }

        if (paymentResponse == null || paymentResponse.status() == null
                || PaymentStatus.PENDING.equals(paymentResponse.status())) {
            return markPaymentUnknown(orderCreatedResponse.id(), sagaInstance, token);
        }

        // Persist recoverable state before changing the remote Order, so a failed
        // completion save can be resolved by looking up this same payment later.
        sagaInstance.setSagaStatus(SagaStatus.PAYMENT_UNKNOWN);
        sagaInstance = sagaInstanceRepository.save(sagaInstance);
        return handlePaymentStatus(orderCreatedResponse.id(), paymentResponse, sagaInstance, token);
    }

    private OrderResponse markPaymentUnknown(Long orderId, SagaInstance sagaInstance, String token) {
        sagaInstance.setSagaStatus(SagaStatus.PAYMENT_UNKNOWN);
        sagaInstance.setNextRetryAt(null);
        sagaInstanceRepository.save(sagaInstance);
        return orderClient.updateOrderStatus(orderId,
                new UpdateOrderStatusRequest(OrderStatus.PAYMENT_UNKNOWN), token);
    }

    public void recoverUnknownPayments(String token) {

        List<SagaInstance> sagas = sagaInstanceRepository.findDueSagas(
                SagaStatus.PAYMENT_UNKNOWN, LocalDateTime.now());

        for (SagaInstance sagaInstance : sagas) {
            try {
                if (sagaInstance.getRetryCount() >= MAX_RECOVERY_ATTEMPTS) {
                    log.warn("Maximum recovery attempts reached for order {}. Moving Saga to manual review.",
                            sagaInstance.getOrderId());
                    sagaInstance.setSagaStatus(SagaStatus.PAYMENT_MANUAL_REVIEW);
                    sagaInstance.setNextRetryAt(null);
                    sagaInstanceRepository.save(sagaInstance);
                    continue;
                }

                sagaInstance = recordRetryAttempt(sagaInstance);
                PaymentResponse paymentResponse;
                try {
                    paymentResponse = paymentClient.getPaymentByOrderId(sagaInstance.getOrderId(), token);
                } catch (PaymentNotFoundException ex) {
                    log.warn("No payment record found for order {}. Keeping Saga unresolved; "
                                    + "automatic payment creation is skipped during recovery.",
                            sagaInstance.getOrderId());
                    continue;
                }

                paymentResponse = reconcilePendingPayment(sagaInstance.getOrderId(), paymentResponse, token);

                if (paymentResponse != null && paymentResponse.status() != null) {
                    handlePaymentStatus(sagaInstance.getOrderId(), paymentResponse, sagaInstance, token);
                }
            } catch (OptimisticLockingFailureException ex) {
                SagaInstance latest = sagaInstanceRepository.findByOrderId(sagaInstance.getOrderId())
                        .orElse(null);
                log.warn("Concurrent recovery update for order {}. Latest Saga state: {}",
                        sagaInstance.getOrderId(), latest == null ? "missing" : latest.getSagaStatus(),
                        ex);
            } catch (Exception ex) {
                log.warn("Recovery failed for order {}. It will remain unresolved and be retried.",
                        sagaInstance.getOrderId(), ex);
            }
        }
    }

    private OrderResponse handlePaymentStatus(
            Long orderId,
            PaymentResponse paymentResponse,
            SagaInstance sagaInstance,
            String token) {

        switch (paymentResponse.status()) {
            case SUCCESS -> {
                OrderResponse order = orderClient.updateOrderStatus(orderId,
                        new UpdateOrderStatusRequest(OrderStatus.CONFIRMED), token);
                completeSaga(sagaInstance);
                log.info("Payment successful for order {}. Saga completed.", orderId);
                return order;
            }
            case FAILED -> {
                OrderResponse order = orderClient.updateOrderStatus(orderId,
                        new UpdateOrderStatusRequest(OrderStatus.PAYMENT_FAILED), token);
                completeSaga(sagaInstance);
                log.info("Payment failed for order {}. Saga completed.", orderId);
                return order;
            }
            case PENDING -> {
                log.info("Payment remains pending for order {}", orderId);
                return null;
            }
        }
        return null;
    }

    private void completeSaga(SagaInstance sagaInstance) {
        sagaInstance.setSagaStatus(SagaStatus.COMPLETED);
        sagaInstance.setNextRetryAt(null);
        sagaInstanceRepository.save(sagaInstance);
    }

    private PaymentResponse reconcilePendingPayment(Long orderId, PaymentResponse paymentResponse, String token) {
        if (paymentResponse != null && paymentResponse.status() == PaymentStatus.PENDING) {
            return paymentClient.reconcilePayment(orderId, token);
        }
        return paymentResponse;
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {

        long delaySeconds = Math.min(
                30L * (1L << (retryCount - 1)),
                300L
        );

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    private SagaInstance  recordRetryAttempt(SagaInstance sagaInstance) {

        int retryCount = sagaInstance.getRetryCount() + 1;

        sagaInstance.setRetryCount(retryCount);
        sagaInstance.setLastRetryAt(LocalDateTime.now());
        sagaInstance.setNextRetryAt(calculateNextRetryTime(retryCount));

        return sagaInstanceRepository.save(sagaInstance);

    }

    public ReconciliationResult reconcileManualReviewSaga(Long orderId, String token) {
        try {
            return transactionTemplate.execute(status -> reconcileManualReviewInTransaction(orderId, token));
        } catch (OptimisticLockingFailureException ex) {
            SagaInstance latest = sagaInstanceRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new IllegalStateException("No Saga found for order " + orderId, ex));
            log.warn("Concurrent Saga update detected for order {}. Latest Saga state is {}.",
                    orderId, latest.getSagaStatus());
            if (latest.getSagaStatus() == SagaStatus.COMPLETED) {
                return ReconciliationResult.COMPLETED;
            }
            if (latest.getSagaStatus() != SagaStatus.PAYMENT_MANUAL_REVIEW) {
                return ReconciliationResult.INVALID_SAGA_STATE;
            }
            return ReconciliationResult.CONCURRENT_UPDATE;
        }
    }

    private ReconciliationResult reconcileManualReviewInTransaction(Long orderId, String token) {
        SagaInstance sagaInstance =
                sagaInstanceRepository.findByOrderIdForUpdate(orderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No Saga found for order " + orderId
                                )
                        );

        if (sagaInstance.getSagaStatus() == SagaStatus.COMPLETED) {
            return ReconciliationResult.COMPLETED;
        }
        if (sagaInstance.getSagaStatus() != SagaStatus.PAYMENT_MANUAL_REVIEW) {
            return ReconciliationResult.INVALID_SAGA_STATE;
        }

        final PaymentResponse paymentResponse;
        try {
            paymentResponse = reconcilePendingPayment(
                    orderId, paymentClient.getPaymentByOrderId(orderId, token), token);
        } catch (PaymentNotFoundException ex) {
            log.warn("No payment found for order {} during manual reconciliation", orderId);
            return ReconciliationResult.PAYMENT_NOT_FOUND;
        } catch (Exception ex) {
            log.error("Could not determine payment status for order {} during manual reconciliation", orderId, ex);
            return ReconciliationResult.SERVICE_UNAVAILABLE;
        }

        if (paymentResponse == null || paymentResponse.status() == null) {
            return ReconciliationResult.SERVICE_UNAVAILABLE;
        }

        log.info("Manual reconciliation payment response for order {}: {}", orderId, paymentResponse);

        if (paymentResponse.status() == PaymentStatus.PENDING) {
            log.warn("Payment is still PENDING for order {}. Keeping Saga in manual review.", orderId);
            return ReconciliationResult.STILL_PENDING;
        }

        handlePaymentStatus(orderId, paymentResponse, sagaInstance, token);

        if (sagaInstance.getSagaStatus() == SagaStatus.COMPLETED) {
            return ReconciliationResult.COMPLETED;
        }

        return ReconciliationResult.SERVICE_UNAVAILABLE;
    }

}
