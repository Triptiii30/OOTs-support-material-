package com.smart.manufacturing.repository;

import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);
    Optional<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.currentStock <= i.minStockLevel")
    List<Inventory> findLowStockInventories();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.currentStock <= i.minStockLevel")
    long countLowStockInventories();
}
