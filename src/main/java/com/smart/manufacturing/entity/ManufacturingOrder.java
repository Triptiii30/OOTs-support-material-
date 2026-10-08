package com.smart.manufacturing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.exception.InvalidOrderStatusException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ManufacturingOrder is the primary aggregate root of the order processing system.
 * Demonstrates OOP Encapsulation, Business Invariants, and Lifecycle Management.
 */
@Entity
@Table(name = "manufacturing_orders")
public class ManufacturingOrder extends BaseEntity {

    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "required_delivery_date", nullable = false)
    private LocalDate requiredDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionTask> tasks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public ManufacturingOrder() {
        this.orderDate = LocalDate.now();
    }

    public ManufacturingOrder(String orderNumber, Customer customer, LocalDate orderDate,
                              LocalDate requiredDeliveryDate, Priority priority, String notes) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.orderDate = (orderDate != null) ? orderDate : LocalDate.now();
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.priority = (priority != null) ? priority : Priority.NORMAL;
        this.notes = notes;
        this.status = OrderStatus.CREATED;
    }

    /**
     * Recalculates order subtotal, tax (8% standard industrial VAT/GST), and grand total.
     * Enforces the business rule that totals are calculated securely on the backend.
     */
    public void recalculateTotals() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item != null && item.getSubtotal() != null) {
                sum = sum.add(item.getSubtotal());
            }
        }
        this.subtotal = sum.setScale(2, RoundingMode.HALF_UP);
        this.taxAmount = this.subtotal.multiply(BigDecimal.valueOf(0.08)).setScale(2, RoundingMode.HALF_UP);
        this.grandTotal = this.subtotal.add(this.taxAmount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Encapsulated method to add an item and maintain bidirectional integrity.
     */
    public void addItem(OrderItem item) {
        if (item != null) {
            this.items.add(item);
            item.setOrder(this);
            recalculateTotals();
        }
    }

    /**
     * Encapsulated method to remove an item and maintain bidirectional integrity.
     */
    public void removeItem(OrderItem item) {
        if (item != null) {
            this.items.remove(item);
            item.setOrder(null);
            recalculateTotals();
        }
    }

    /**
     * Validates and applies state transitions according to manufacturing lifecycle rules.
     */
    public void transitionTo(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusException(this.status, newStatus);
        }
        this.status = newStatus;
    }

    public boolean isDelayed() {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            return false;
        }
        return requiredDeliveryDate != null && LocalDate.now().isAfter(requiredDeliveryDate);
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    // Getters and Setters
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getRequiredDeliveryDate() {
        return requiredDeliveryDate;
    }

    public void setRequiredDeliveryDate(LocalDate requiredDeliveryDate) {
        this.requiredDeliveryDate = requiredDeliveryDate;
    }

    public LocalDate getRequiredDate() {
        return requiredDeliveryDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDeliveryDate = requiredDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items.clear();
        if (items != null) {
            for (OrderItem item : items) {
                addItem(item);
            }
        }
        recalculateTotals();
    }

    public List<ProductionTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(ProductionTask task) {
        if (task != null) {
            this.tasks.add(task);
            task.setOrder(this);
        }
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public void addStatusHistory(OrderStatusHistory history) {
        if (history != null) {
            this.statusHistory.add(history);
            history.setOrder(this);
        }
    }
}
