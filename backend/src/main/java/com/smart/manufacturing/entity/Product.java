package com.smart.manufacturing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Product entity representing manufactured items.
 * Demonstrates Encapsulation and Domain Logic.
 */
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", nullable = false, length = 80)
    private String category;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "production_duration_hours", nullable = false)
    private int productionDurationHours;

    @Column(name = "min_stock_level", nullable = false)
    private int minStockLevel;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @JsonIgnore
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;

    public Product() {
    }

    public Product(String productCode, String name, String description, String category,
                   BigDecimal unitPrice, int productionDurationHours, int minStockLevel) {
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitPrice = unitPrice;
        this.productionDurationHours = productionDurationHours;
        this.minStockLevel = minStockLevel;
        this.active = true;
    }

    public boolean isLowStock() {
        if (inventory == null) return false;
        return inventory.getCurrentStock() <= minStockLevel;
    }

    public int getCurrentStock() {
        return inventory != null ? inventory.getCurrentStock() : 0;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSku() {
        return productCode;
    }

    public void setSku(String sku) {
        this.productCode = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getProductionDurationHours() {
        return productionDurationHours;
    }

    public void setProductionDurationHours(int productionDurationHours) {
        this.productionDurationHours = productionDurationHours;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
