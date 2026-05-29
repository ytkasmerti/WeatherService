package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.UserSubscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByEmail(String email);

    Optional<UserSubscription> findByIdAndEmail(Long id, String email);

    void deleteByEmail(String email);

    boolean existsByEmailAndCity(String email, String city);
}