package com.smart.manufacturing.enums;

public enum Priority {
    LOW(1, "Low", "badge-secondary"),
    NORMAL(2, "Normal", "badge-info"),
    HIGH(3, "High", "badge-warning"),
    URGENT(4, "Urgent", "badge-danger");

    private final int level;
    private final String displayName;
    private final String badgeClass;

    Priority(int level, String displayName, String badgeClass) {
        this.level = level;
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
