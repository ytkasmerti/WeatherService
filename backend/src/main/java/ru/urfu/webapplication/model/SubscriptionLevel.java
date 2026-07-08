package ru.urfu.webapplication.model;

public enum SubscriptionLevel {
    FREE(0),
    BASIC(1),
    PREMIUM(2);

    private final int priority;

    SubscriptionLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
