package com.educba.onlinebookstore.orchestratorservice.controller;

import com.educba.onlinebookstore.orchestratorservice.security.ServiceTokenProvider;
import com.educba.onlinebookstore.orchestratorservice.service.OrchestratorService;
import com.educba.onlinebookstore.orchestratorservice.dto.ReconciliationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/sagas")
public class SagaReconciliationController {

    private final OrchestratorService orchestratorService;
    private final ServiceTokenProvider serviceTokenProvider;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{orderId}/reconcile")
    public ResponseEntity<ReconciliationResult> reconcileSaga(
            @PathVariable Long orderId) {

        String token = serviceTokenProvider.generateToken();

        ReconciliationResult result = orchestratorService.reconcileManualReviewSaga(
                orderId,
                token
        );

        return switch (result) {
            case COMPLETED, STILL_PENDING -> ResponseEntity.ok(result);
            case PAYMENT_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            case SERVICE_UNAVAILABLE -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
            case INVALID_SAGA_STATE -> ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            case CONCURRENT_UPDATE -> ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        };
    }

}
