package com.microservices.onlinebookstore.orchestratorservice.scheduler;

import com.microservices.onlinebookstore.orchestratorservice.security.ServiceTokenProvider;
import com.microservices.onlinebookstore.orchestratorservice.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaRecoveryScheduler {

    private final OrchestratorService orchestratorService;
    private final ServiceTokenProvider serviceTokenProvider;

    @Scheduled(fixedDelay = 30000)
    public void recoverUnknownPayments() {

        log.info("Starting Saga recovery for PAYMENT_UNKNOWN orders");

        String token = serviceTokenProvider.generateToken();

        try {
            orchestratorService.recoverUnknownPayments(token);
        } catch (Exception ex) {
            log.error("Saga recovery run failed; the next scheduled run will retry", ex);
        }
    }
}
