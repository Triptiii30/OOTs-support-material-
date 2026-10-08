package com.smart.manufacturing.enums;

public enum ProductionTaskStatus {
    PENDING("Pending", "badge-secondary"),
    SCHEDULED("Scheduled", "badge-warning"),
    IN_PROGRESS("In Progress", "badge-primary"),
    PAUSED("Paused", "badge-warning"),
    COMPLETED("Completed", "badge-success"),
    CANCELLED("Cancelled", "badge-danger");

    private final String displayName;
    private final String badgeClass;

    ProductionTaskStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
