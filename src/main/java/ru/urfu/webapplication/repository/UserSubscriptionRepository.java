package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.UserSubscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    List<UserSubscription> findByUser(User user);

    Optional<UserSubscription> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);

    boolean existsByUserAndCity(User user, String city);
}