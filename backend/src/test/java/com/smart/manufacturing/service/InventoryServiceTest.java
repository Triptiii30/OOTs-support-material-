package com.smart.manufacturing.service;

import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.exception.InsufficientInventoryException;
import com.smart.manufacturing.repository.InventoryRepository;
import com.smart.manufacturing.repository.InventoryTransactionRepository;
import com.smart.manufacturing.repository.ProductRepository;
import com.smart.manufacturing.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Service Unit Tests")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = new Product("SKU-100", "Servo Motor", "Precision Motor", "Electronics",
                BigDecimal.valueOf(250.00), 4, 10);
        testProduct.setId(1L);

        testInventory = new Inventory(testProduct, 50, 10);
        testInventory.setId(10L);
    }

    @Test
    @DisplayName("Stock addition increases current stock and records transaction")
    void testAddStockSuccess() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryService.addStock(1L, 25, "Restock batch", "admin");

        assertEquals(75, result.getCurrentStock());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Stock deduction decreases current stock")
    void testRemoveStockSuccess() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryService.removeStock(1L, 20, "Scrap removal", "admin");

        assertEquals(30, result.getCurrentStock());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Stock deduction throws InsufficientInventoryException when requested quantity exceeds available stock")
    void testRemoveStockInsufficientThrowsException() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        assertThrows(InsufficientInventoryException.class, () ->
                inventoryService.removeStock(1L, 100, "Excess withdrawal", "admin")
        );
    }

    @Test
    @DisplayName("Stock allocation reserves units and verifies available stock")
    void testAllocateAndReleaseStock() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        // Allocate 30 units
        inventoryService.allocateStockForOrder(1L, 30, 501L, "staff");
        assertEquals(30, testInventory.getAllocatedStock());
        assertEquals(20, testInventory.getAvailableStock());

        // Release 10 units
        inventoryService.releaseStockForOrder(1L, 10, 501L, "staff");
        assertEquals(20, testInventory.getAllocatedStock());
        assertEquals(30, testInventory.getAvailableStock());
    }
}
