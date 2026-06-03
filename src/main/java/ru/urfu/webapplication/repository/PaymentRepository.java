package ru.urfu.webapplication.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.Payment;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByApiKeyAndStatusAndExpiresAtAfter(String apiKey, PaymentStatus status, LocalDateTime now);

    Page<Payment> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}