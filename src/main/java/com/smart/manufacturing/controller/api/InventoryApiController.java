package com.smart.manufacturing.controller.api;

import com.smart.manufacturing.dto.ApiResponse;
import com.smart.manufacturing.dto.InventoryAdjustmentDto;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.InventoryTransaction;
import com.smart.manufacturing.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryApiController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryApiController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Inventory>>> getAllInventory() {
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        return ResponseEntity.ok(ApiResponse.ok("Inventory list retrieved", inventoryList));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> getInventoryByProduct(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(ApiResponse.ok("Product inventory retrieved", inventory));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INVENTORY_COORDINATOR')")
    public ResponseEntity<ApiResponse<Inventory>> adjustStock(@Valid @RequestBody InventoryAdjustmentDto dto,
                                                              Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "API User";
        Inventory updated = inventoryService.adjustStock(dto, username);
        return ResponseEntity.ok(ApiResponse.ok("Stock adjustment completed", updated));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<InventoryTransaction>>> getTransactions(
            @RequestParam(required = false) Long productId) {
        List<InventoryTransaction> transactions = (productId != null) ?
                inventoryService.getTransactionsByProduct(productId) :
                inventoryService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.ok("Transaction history retrieved", transactions));
    }
}
