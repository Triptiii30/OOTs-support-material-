package com.smart.manufacturing.entity;

import jakarta.persistence.*;

/**
 * AdminUser — Specialization of User demonstrating Inheritance.
 * Demonstrates: extends, super keyword, method overriding, @Override
 */
@Entity
@DiscriminatorValue("ADMIN_USER")
public class AdminUser extends User {

    @Column(name = "admin_level")
    private int adminLevel;

    @Column(name = "can_manage_users")
    private boolean canManageUsers;

    public AdminUser() {
        super();
        this.adminLevel = 1;
        this.canManageUsers = true;
    }

    public AdminUser(String username, String email, String password) {
        super(username, email, password);
        this.adminLevel = 1;
        this.canManageUsers = true;
    }

    @Override
    public String getDisplayRole() {
        return "System Administrator (Level " + this.adminLevel + ")";
    }

    @Override
    public boolean hasPermission(String action) {
        return true; // Admin has all permissions
    }

    public int getAdminLevel() { return adminLevel; }
    public void setAdminLevel(int adminLevel) { this.adminLevel = adminLevel; }
    public boolean isCanManageUsers() { return canManageUsers; }
    public void setCanManageUsers(boolean canManageUsers) { this.canManageUsers = canManageUsers; }
}
