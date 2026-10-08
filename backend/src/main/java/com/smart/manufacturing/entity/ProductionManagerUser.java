package com.smart.manufacturing.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PROD_MANAGER_USER")
public class ProductionManagerUser extends User {

    @Column(name = "department")
    private String department;

    @Column(name = "max_concurrent_tasks")
    private int maxConcurrentTasks;

    public ProductionManagerUser() {
        super();
        this.maxConcurrentTasks = 10;
        this.department = "Production";
    }

    public ProductionManagerUser(String username, String email, String password) {
        super(username, email, password);
        this.maxConcurrentTasks = 10;
        this.department = "Production";
    }

    @Override
    public String getDisplayRole() {
        return "Production Manager - " + this.department;
    }

    @Override
    public boolean hasPermission(String action) {
        return action.startsWith("PRODUCTION_") || action.startsWith("SCHEDULE_") || action.startsWith("VIEW_");
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getMaxConcurrentTasks() { return maxConcurrentTasks; }
    public void setMaxConcurrentTasks(int v) { this.maxConcurrentTasks = v; }
}
