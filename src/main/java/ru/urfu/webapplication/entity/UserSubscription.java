package ru.urfu.webapplication.entity;

import lombok.Data;

@Data
public class UserSubscription {
    private String email;
    private String city;
    private boolean notifyHeat;
    private boolean notifyCold;
    private boolean notifyWind;
    private boolean notifyPrecipitation;
}