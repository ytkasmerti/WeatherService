package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.Payment;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByApiKeyAndIsConfirmedFalse(String apiKey);
    Optional<Payment> findByApiKeyAndStatusAndIsConfirmedFalse(String apiKey, String status);
}