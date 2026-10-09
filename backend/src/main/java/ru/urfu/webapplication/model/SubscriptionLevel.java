package ru.urfu.webapplication.model;

import lombok.Getter;

@Getter
public enum SubscriptionLevel {
    FREE(0),
    BASIC(1),
    PREMIUM(2);

    private final int priority;

    SubscriptionLevel(int priority) {
        this.priority = priority;
    }

}