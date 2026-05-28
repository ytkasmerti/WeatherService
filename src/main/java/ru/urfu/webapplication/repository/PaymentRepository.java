package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.Payment;
import ru.urfu.webapplication.model.PaymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByApiKeyAndStatus(String apiKey, PaymentStatus status);

    List<Payment> findAllByEmailOrderByCreatedAtDesc(String email);
}