package com.smart.manufacturing.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    private long totalCustomers;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long inProductionOrders;
    private long completedOrders;
    private long delayedOrders;
    private long lowStockProducts;
    private long highPriorityOrders;

    private Map<String, Long> ordersByStatus = new HashMap<>();
    private Map<String, Long> ordersByPriority = new HashMap<>();
    private Map<String, Long> inventoryByCategory = new HashMap<>();

    private List<OrderResponseDto> recentOrders;
    private List<OrderResponseDto> priorityQueueOrders;

    public DashboardStatsDto() {
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getInProductionOrders() {
        return inProductionOrders;
    }

    public void setInProductionOrders(long inProductionOrders) {
        this.inProductionOrders = inProductionOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public long getDelayedOrders() {
        return delayedOrders;
    }

    public void setDelayedOrders(long delayedOrders) {
        this.delayedOrders = delayedOrders;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public long getHighPriorityOrders() {
        return highPriorityOrders;
    }

    public void setHighPriorityOrders(long highPriorityOrders) {
        this.highPriorityOrders = highPriorityOrders;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public Map<String, Long> getOrdersByPriority() {
        return ordersByPriority;
    }

    public void setOrdersByPriority(Map<String, Long> ordersByPriority) {
        this.ordersByPriority = ordersByPriority;
    }

    public Map<String, Long> getInventoryByCategory() {
        return inventoryByCategory;
    }

    public void setInventoryByCategory(Map<String, Long> inventoryByCategory) {
        this.inventoryByCategory = inventoryByCategory;
    }

    public List<OrderResponseDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderResponseDto> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<OrderResponseDto> getPriorityQueueOrders() {
        return priorityQueueOrders;
    }

    public void setPriorityQueueOrders(List<OrderResponseDto> priorityQueueOrders) {
        this.priorityQueueOrders = priorityQueueOrders;
    }
}
