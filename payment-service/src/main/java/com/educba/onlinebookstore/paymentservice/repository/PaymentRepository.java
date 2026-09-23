package com.educba.onlinebookstore.paymentservice.repository;

import com.educba.onlinebookstore.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);


}
