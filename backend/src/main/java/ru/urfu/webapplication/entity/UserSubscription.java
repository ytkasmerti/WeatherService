package ru.urfu.webapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String city;

    @Column(name = "notify_heat")
    private boolean notifyHeat;

    @Column(name = "notify_cold")
    private boolean notifyCold;

    @Column(name = "notify_wind")
    private boolean notifyWind;

    @Column(name = "notify_precip")
    private boolean notifyPrecipitation;
}