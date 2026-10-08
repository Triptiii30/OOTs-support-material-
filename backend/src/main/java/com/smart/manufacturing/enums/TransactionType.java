package com.smart.manufacturing.enums;

public enum TransactionType {
    STOCK_IN("Stock Added"),
    STOCK_OUT("Stock Removed"),
    ALLOCATED("Allocated to Order"),
    RELEASED("Released from Cancelled Order"),
    ADJUSTMENT("Manual Adjustment");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
