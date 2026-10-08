package com.smart.manufacturing.enums;

public enum OrderStatus {
    CREATED("Order Created", "badge-secondary"),
    VALIDATED("Validated", "badge-info"),
    APPROVED("Approved", "badge-primary"),
    SCHEDULED("Scheduled", "badge-warning"),
    IN_PROGRESS("In Production", "badge-primary"),
    COMPLETED("Completed", "badge-success"),
    CANCELLED("Cancelled", "badge-danger");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public boolean canTransitionTo(OrderStatus target) {
        if (this == CANCELLED || this == COMPLETED) {
            return false;
        }
        if (target == CANCELLED) {
            return true;
        }
        return switch (this) {
            case CREATED -> target == VALIDATED;
            case VALIDATED -> target == APPROVED;
            case APPROVED -> target == SCHEDULED;
            case SCHEDULED -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == COMPLETED;
            default -> false;
        };
    }
}
