package com.smart.manufacturing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.enums.ProductionTaskStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ProductionTask represents work units dispatched to shop floor teams.
 * Demonstrates OOP Encapsulation and status integrity.
 */
@Entity
@Table(name = "production_tasks")
public class ProductionTask extends BaseEntity {

    @Column(name = "task_code", unique = true, nullable = false, length = 50)
    private String taskCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private ManufacturingOrder order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ProductionSchedule schedule;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "assigned_team", nullable = false, length = 100)
    private String assignedTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private Priority priority = Priority.NORMAL;

    @Column(name = "planned_start_date", nullable = false)
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date", nullable = false)
    private LocalDateTime plannedEndDate;

    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductionTaskStatus status = ProductionTaskStatus.PENDING;

    @Column(name = "progress_percentage", nullable = false)
    private int progressPercentage = 0;

    @Column(name = "notes", length = 500)
    private String notes;

    public ProductionTask() {
    }

    public ProductionTask(String taskCode, ManufacturingOrder order, Product product, int quantity,
                          String assignedTeam, Priority priority, LocalDateTime plannedStartDate,
                          LocalDateTime plannedEndDate) {
        this.taskCode = taskCode;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.assignedTeam = assignedTeam;
        this.priority = priority;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.status = ProductionTaskStatus.SCHEDULED;
        this.progressPercentage = 0;
    }

    public void updateProgress(int progress, ProductionTaskStatus newStatus) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
        this.progressPercentage = progress;
        this.status = newStatus;
        if (progress == 100 && newStatus == ProductionTaskStatus.COMPLETED) {
            this.actualCompletionDate = LocalDateTime.now();
        }
    }

    public boolean isDelayed() {
        if (status == ProductionTaskStatus.COMPLETED) return false;
        return plannedEndDate != null && LocalDateTime.now().isAfter(plannedEndDate);
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public ManufacturingOrder getOrder() {
        return order;
    }

    public void setOrder(ManufacturingOrder order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductionSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(ProductionSchedule schedule) {
        this.schedule = schedule;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDateTime getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(LocalDateTime plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public LocalDateTime getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(LocalDateTime plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public LocalDateTime getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(LocalDateTime actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }

    public ProductionTaskStatus getStatus() {
        return status;
    }

    public void setStatus(ProductionTaskStatus status) {
        this.status = status;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
