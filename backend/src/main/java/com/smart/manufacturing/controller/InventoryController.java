package com.smart.manufacturing.controller;

import com.smart.manufacturing.dto.InventoryAdjustmentDto;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.InventoryTransaction;
import com.smart.manufacturing.enums.TransactionType;
import com.smart.manufacturing.service.InventoryService;
import com.smart.manufacturing.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    @Autowired
    public InventoryController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @GetMapping
    public String viewInventory(Model model) {
        List<Inventory> inventories = inventoryService.getAllInventory();
        List<InventoryTransaction> transactions = inventoryService.getAllTransactions();

        model.addAttribute("inventories", inventories);
        model.addAttribute("transactions", transactions);
        model.addAttribute("lowStockCount", inventoryService.countLowStockInventories());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("adjustmentDto", new InventoryAdjustmentDto());
        model.addAttribute("transactionTypes", TransactionType.values());

        return "inventory/index";
    }

    @PostMapping("/adjust")
    public String adjustStock(@Valid @ModelAttribute("adjustmentDto") InventoryAdjustmentDto dto,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            inventoryService.adjustStock(dto, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Inventory adjusted successfully: " + dto.getTransactionType().getDescription() + " (" + dto.getQuantity() + " units)");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/inventory";
    }
}
