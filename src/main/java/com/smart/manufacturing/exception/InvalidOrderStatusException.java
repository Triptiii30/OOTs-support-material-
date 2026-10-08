package com.smart.manufacturing.exception;

import com.smart.manufacturing.enums.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(OrderStatus current, OrderStatus target) {
        super("Invalid order status transition from '" + current + "' to '" + target + "'");
    }

    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
