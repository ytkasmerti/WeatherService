package ru.urfu.webapplication.model;

public enum PaymentStatus {
    PENDING,   //ожидает оплаты
    CONFIRMED, //подтверждён
    EXPIRED,   //просрочен
    FAILED     //отклонён
}