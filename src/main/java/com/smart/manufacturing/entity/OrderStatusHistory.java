package com.smart.manufacturing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smart.manufacturing.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * OrderStatusHistory tracks the complete lifecycle audit log of an order.
 */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private ManufacturingOrder order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private OrderStatus newStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public OrderStatusHistory() {
        this.changedAt = LocalDateTime.now();
    }

    public OrderStatusHistory(ManufacturingOrder order, OrderStatus previousStatus, OrderStatus newStatus,
                              User changedBy, String comments) {
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.comments = comments;
        this.changedAt = LocalDateTime.now();
    }

    public ManufacturingOrder getOrder() {
        return order;
    }

    public void setOrder(ManufacturingOrder order) {
        this.order = order;
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(OrderStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
