package com.example.orderservice.entity;

public enum OrderStatus {

    CREATED(10),
    CONFIRMED(20),
    CANCELLED(30),
    COMPLETED(40);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Invalid OrderStatus code: " + code
        );
    }
}