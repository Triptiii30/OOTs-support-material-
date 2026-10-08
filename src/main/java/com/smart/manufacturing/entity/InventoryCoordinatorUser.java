package com.smart.manufacturing.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("INV_COORDINATOR_USER")
public class InventoryCoordinatorUser extends User {

    @Column(name = "warehouse_zone")
    private String warehouseZone;

    public InventoryCoordinatorUser() {
        super();
        this.warehouseZone = "A";
    }

    public InventoryCoordinatorUser(String username, String email, String password) {
        super(username, email, password);
        this.warehouseZone = "A";
    }

    @Override
    public String getDisplayRole() {
        return "Inventory Coordinator - Zone " + this.warehouseZone;
    }

    @Override
    public boolean hasPermission(String action) {
        return action.startsWith("INVENTORY_") || action.startsWith("PRODUCT_") || action.startsWith("VIEW_");
    }

    public String getWarehouseZone() { return warehouseZone; }
    public void setWarehouseZone(String warehouseZone) { this.warehouseZone = warehouseZone; }
}
