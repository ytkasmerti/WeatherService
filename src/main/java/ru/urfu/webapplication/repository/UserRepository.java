package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByApiKey(String apiKey);

    boolean existsByEmail(String email);

    List<User> findBySubscriptionExpiresAtBefore(LocalDateTime dateTime);

    List<User> findBySubscriptionExpiresAtBetween(LocalDateTime start, LocalDateTime end);
}