package com.smart.manufacturing.entity;

import com.smart.manufacturing.exception.InsufficientInventoryException;
import jakarta.persistence.*;

/**
 * Inventory entity tracking stock levels, allocations, and thresholds.
 * Encapsulates stock validation rules to prevent negative inventory and mismatches.
 */
@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "current_stock", nullable = false)
    private int currentStock = 0;

    @Column(name = "allocated_stock", nullable = false)
    private int allocatedStock = 0;

    @Column(name = "min_stock_level", nullable = false)
    private int minStockLevel = 10;

    public Inventory() {
    }

    public Inventory(Product product, int currentStock, int minStockLevel) {
        this.product = product;
        this.currentStock = currentStock;
        this.allocatedStock = 0;
        this.minStockLevel = minStockLevel;
    }

    /**
     * Available stock = physical stock minus stock currently reserved/allocated for orders.
     */
    public int getAvailableStock() {
        return Math.max(0, currentStock - allocatedStock);
    }

    public boolean isLowStock() {
        return currentStock <= minStockLevel;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be strictly greater than zero");
        }
        this.currentStock += quantity;
    }

    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to deduct must be strictly greater than zero");
        }
        if (this.currentStock < quantity) {
            throw new InsufficientInventoryException(
                "Insufficient inventory for product '" + (product != null ? product.getName() : "Item") +
                "'. Available: " + this.currentStock + ", Requested: " + quantity
            );
        }
        this.currentStock -= quantity;
    }

    public void allocateStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to allocate must be strictly greater than zero");
        }
        if (getAvailableStock() < quantity) {
            throw new InsufficientInventoryException(
                "Insufficient inventory for product '" + (product != null ? product.getName() : "Item") +
                "'. Available: " + getAvailableStock() + ", Requested: " + quantity
            );
        }
        this.allocatedStock += quantity;
    }

    public void releaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to release must be strictly greater than zero");
        }
        this.allocatedStock = Math.max(0, this.allocatedStock - quantity);
    }

    public void fulfillAllocatedStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be strictly greater than zero");
        }
        deductStock(quantity);
        releaseStock(quantity);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = Math.max(0, currentStock);
    }

    public int getAllocatedStock() {
        return allocatedStock;
    }

    public void setAllocatedStock(int allocatedStock) {
        this.allocatedStock = Math.max(0, allocatedStock);
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = Math.max(0, minStockLevel);
    }
}
