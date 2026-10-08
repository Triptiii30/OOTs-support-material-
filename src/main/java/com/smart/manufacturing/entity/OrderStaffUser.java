package com.smart.manufacturing.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ORDER_STAFF_USER")
public class OrderStaffUser extends User {

    @Column(name = "orders_processed_today")
    private int ordersProcessedToday;

    public OrderStaffUser() {
        super();
        this.ordersProcessedToday = 0;
    }

    public OrderStaffUser(String username, String email, String password) {
        super(username, email, password);
    }

    @Override
    public String getDisplayRole() {
        return "Order Processing Staff";
    }

    @Override
    public boolean hasPermission(String action) {
        return action.startsWith("ORDER_") || action.startsWith("CUSTOMER_") || action.startsWith("VIEW_");
    }

    public int getOrdersProcessedToday() { return ordersProcessedToday; }
    public void setOrdersProcessedToday(int v) { this.ordersProcessedToday = v; }
}
