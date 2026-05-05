package ru.urfu.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.webapplication.entity.WeatherAlert;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {
}