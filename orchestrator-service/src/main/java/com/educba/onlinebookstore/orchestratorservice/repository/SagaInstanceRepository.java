package com.educba.onlinebookstore.orchestratorservice.repository;

import com.educba.onlinebookstore.orchestratorservice.constants.SagaStatus;
import com.educba.onlinebookstore.orchestratorservice.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long>  {
    List<SagaInstance> findBySagaStatus(SagaStatus sagaStatus);
    List<SagaInstance> findBySagaStatusAndNextRetryAtLessThanEqual(SagaStatus sagaStatus,
            LocalDateTime nextRetryAt
    );
    @Query("""
        SELECT s
        FROM SagaInstance s
        WHERE s.sagaStatus = :status
          AND (s.nextRetryAt IS NULL OR s.nextRetryAt <= :now)
        """)
    List<SagaInstance> findDueSagas(
            @Param("status") SagaStatus status,
            @Param("now") LocalDateTime now
    );

    List<SagaInstance> findBySagaStatusAndNextRetryAtIsNull(
            SagaStatus sagaStatus
    );

    Optional<SagaInstance> findByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SagaInstance s WHERE s.orderId = :orderId")
    Optional<SagaInstance> findByOrderIdForUpdate(@Param("orderId") Long orderId);
}
