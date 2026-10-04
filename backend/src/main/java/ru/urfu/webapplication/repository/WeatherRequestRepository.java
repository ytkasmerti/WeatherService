package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.User;
import ru.urfu.webapplication.entity.WeatherRequest;

import java.time.LocalDateTime;

@Repository
public interface WeatherRequestRepository extends JpaRepository<WeatherRequest, Long> {

    @Query("SELECT COUNT(wr) FROM WeatherRequest wr WHERE wr.user = :user AND wr.requestTime >= :since")
    long countRequestsByUserInLast24Hours(@Param("user") User user, @Param("since") LocalDateTime since);
}