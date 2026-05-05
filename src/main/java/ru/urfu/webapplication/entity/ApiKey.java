package ru.urfu.webapplication.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.urfu.webapplication.model.SubscriptionLevel;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Data
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_value", unique = true, nullable = false)
    private String keyValue;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private SubscriptionLevel subscriptionLevel;

    private LocalDateTime createdAt;
    private Boolean isActive;
}
