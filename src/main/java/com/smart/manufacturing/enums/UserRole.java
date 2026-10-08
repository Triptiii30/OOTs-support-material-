package com.smart.manufacturing.enums;

public enum UserRole {
    ROLE_ADMIN("Admin", "Full system access"),
    ROLE_PRODUCTION_MANAGER("Production Manager", "Manage production orders and scheduling"),
    ROLE_ORDER_STAFF("Order Staff", "Customer and order entry processing"),
    ROLE_INVENTORY_COORDINATOR("Inventory Coordinator", "Manage stock, adjustments and transactions"),
    ROLE_SUPERVISOR("Supervisor", "Monitoring dashboards and analytical reports");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
