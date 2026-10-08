package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.InventoryAdjustmentDto;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.InventoryTransaction;

import java.util.List;

public interface InventoryService {
    List<Inventory> getAllInventory();
    Inventory getInventoryByProductId(Long productId);
    Inventory addStock(Long productId, int quantity, String notes, String performedBy);
    Inventory removeStock(Long productId, int quantity, String notes, String performedBy);
    Inventory adjustStock(InventoryAdjustmentDto dto, String performedBy);
    void validateStockForOrder(Long productId, int quantity);
    void allocateStockForOrder(Long productId, int quantity, Long orderId, String performedBy);
    void releaseStockForOrder(Long productId, int quantity, Long orderId, String performedBy);
    void fulfillStockForOrder(Long productId, int quantity, Long orderId, String performedBy);
    List<Inventory> getLowStockInventories();
    long countLowStockInventories();
    List<InventoryTransaction> getTransactionsByProduct(Long productId);
    List<InventoryTransaction> getAllTransactions();
}
