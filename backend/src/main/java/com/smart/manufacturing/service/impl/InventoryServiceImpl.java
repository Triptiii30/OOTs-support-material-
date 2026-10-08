package com.smart.manufacturing.service.impl;

import com.smart.manufacturing.dto.InventoryAdjustmentDto;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.InventoryTransaction;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.enums.TransactionType;
import com.smart.manufacturing.exception.InsufficientInventoryException;
import com.smart.manufacturing.exception.ProductNotFoundException;
import com.smart.manufacturing.repository.InventoryRepository;
import com.smart.manufacturing.repository.InventoryTransactionRepository;
import com.smart.manufacturing.repository.ProductRepository;
import com.smart.manufacturing.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryTransactionRepository transactionRepository,
                                ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ProductNotFoundException(productId));
                    Inventory newInv = new Inventory(product, 0, product.getMinStockLevel());
                    return inventoryRepository.save(newInv);
                });
    }

    @Override
    public Inventory addStock(Long productId, int quantity, String notes, String performedBy) {
        log.info("Adding {} units to product ID: {} by {}", quantity, productId, performedBy);
        Inventory inventory = getInventoryByProductId(productId);
        inventory.addStock(quantity);
        Inventory saved = inventoryRepository.save(inventory);

        InventoryTransaction tx = new InventoryTransaction(
                inventory.getProduct(),
                TransactionType.STOCK_IN,
                quantity,
                inventory.getCurrentStock(),
                null,
                notes != null ? notes : "Restock addition",
                performedBy
        );
        transactionRepository.save(tx);
        return saved;
    }

    @Override
    public Inventory removeStock(Long productId, int quantity, String notes, String performedBy) {
        log.info("Removing {} units from product ID: {} by {}", quantity, productId, performedBy);
        Inventory inventory = getInventoryByProductId(productId);
        inventory.deductStock(quantity);
        Inventory saved = inventoryRepository.save(inventory);

        InventoryTransaction tx = new InventoryTransaction(
                inventory.getProduct(),
                TransactionType.STOCK_OUT,
                quantity,
                inventory.getCurrentStock(),
                null,
                notes != null ? notes : "Stock removal",
                performedBy
        );
        transactionRepository.save(tx);
        return saved;
    }

    @Override
    public Inventory adjustStock(InventoryAdjustmentDto dto, String performedBy) {
        if (dto.getTransactionType() == TransactionType.STOCK_IN) {
            return addStock(dto.getProductId(), dto.getQuantity(), dto.getNotes(), performedBy);
        } else if (dto.getTransactionType() == TransactionType.STOCK_OUT) {
            return removeStock(dto.getProductId(), dto.getQuantity(), dto.getNotes(), performedBy);
        } else {
            Inventory inventory = getInventoryByProductId(dto.getProductId());
            inventory.setCurrentStock(dto.getQuantity());
            Inventory saved = inventoryRepository.save(inventory);

            InventoryTransaction tx = new InventoryTransaction(
                    inventory.getProduct(),
                    TransactionType.ADJUSTMENT,
                    dto.getQuantity(),
                    inventory.getCurrentStock(),
                    null,
                    dto.getNotes() != null ? dto.getNotes() : "Inventory audit adjustment",
                    performedBy
            );
            transactionRepository.save(tx);
            return saved;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateStockForOrder(Long productId, int quantity) {
        Inventory inventory = getInventoryByProductId(productId);
        if (inventory.getAvailableStock() < quantity) {
            throw new InsufficientInventoryException(
                    "Insufficient inventory for product '" + inventory.getProduct().getName() +
                    "'. Available stock: " + inventory.getAvailableStock() +
                    ", Required quantity: " + quantity
            );
        }
    }

    @Override
    public void allocateStockForOrder(Long productId, int quantity, Long orderId, String performedBy) {
        log.info("Allocating {} units of product ID: {} for order ID: {}", quantity, productId, orderId);
        Inventory inventory = getInventoryByProductId(productId);
        inventory.allocateStock(quantity);
        inventoryRepository.save(inventory);

        InventoryTransaction tx = new InventoryTransaction(
                inventory.getProduct(),
                TransactionType.ALLOCATED,
                quantity,
                inventory.getCurrentStock(),
                orderId,
                "Allocated for Order #" + orderId,
                performedBy
        );
        transactionRepository.save(tx);
    }

    @Override
    public void releaseStockForOrder(Long productId, int quantity, Long orderId, String performedBy) {
        log.info("Releasing {} allocated units of product ID: {} for order ID: {}", quantity, productId, orderId);
        Inventory inventory = getInventoryByProductId(productId);
        inventory.releaseStock(quantity);
        inventoryRepository.save(inventory);

        InventoryTransaction tx = new InventoryTransaction(
                inventory.getProduct(),
                TransactionType.RELEASED,
                quantity,
                inventory.getCurrentStock(),
                orderId,
                "Released from Order #" + orderId,
                performedBy
        );
        transactionRepository.save(tx);
    }

    @Override
    public void fulfillStockForOrder(Long productId, int quantity, Long orderId, String performedBy) {
        log.info("Fulfilling and deducting {} units of product ID: {} for order ID: {}", quantity, productId, orderId);
        Inventory inventory = getInventoryByProductId(productId);
        inventory.fulfillAllocatedStock(quantity);
        inventoryRepository.save(inventory);

        InventoryTransaction tx = new InventoryTransaction(
                inventory.getProduct(),
                TransactionType.STOCK_OUT,
                quantity,
                inventory.getCurrentStock(),
                orderId,
                "Order #" + orderId + " completed and fulfilled",
                performedBy
        );
        transactionRepository.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockInventories() {
        return inventoryRepository.findLowStockInventories();
    }

    @Override
    @Transactional(readOnly = true)
    public long countLowStockInventories() {
        return inventoryRepository.countLowStockInventories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsByProduct(Long productId) {
        return transactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }
}
