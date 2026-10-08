package com.smart.manufacturing.entity;

import com.smart.manufacturing.enums.TransactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * InventoryTransaction tracks audit records of all stock movements.
 */
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "stock_after", nullable = false)
    private int stockAfter;

    @Column(name = "reference_order_id")
    private Long referenceOrderId;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    public InventoryTransaction() {
    }

    public InventoryTransaction(Product product, TransactionType transactionType, int quantity,
                                int stockAfter, Long referenceOrderId, String notes, String performedBy) {
        this.product = product;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.stockAfter = stockAfter;
        this.referenceOrderId = referenceOrderId;
        this.notes = notes;
        this.performedBy = performedBy;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getStockAfter() {
        return stockAfter;
    }

    public void setStockAfter(int stockAfter) {
        this.stockAfter = stockAfter;
    }

    public Long getReferenceOrderId() {
        return referenceOrderId;
    }

    public void setReferenceOrderId(Long referenceOrderId) {
        this.referenceOrderId = referenceOrderId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
}
