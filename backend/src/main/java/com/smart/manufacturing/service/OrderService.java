package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.OrderRequestDto;
import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;

import java.util.List;

public interface OrderService {
    List<ManufacturingOrder> getAllOrders();
    ManufacturingOrder getOrderById(Long id);
    ManufacturingOrder getOrderByNumber(String orderNumber);
    ManufacturingOrder createOrder(OrderRequestDto requestDto, String username);
    ManufacturingOrder updateOrder(Long id, OrderRequestDto requestDto, String username);
    void cancelOrder(Long id, String reason, String username);

    // Lifecycle transitions
    ManufacturingOrder validateOrder(Long id, String username);
    ManufacturingOrder approveOrder(Long id, String username);
    ManufacturingOrder scheduleOrder(Long id, String username);
    ManufacturingOrder startProduction(Long id, String username);
    ManufacturingOrder completeOrder(Long id, String username);

    List<ManufacturingOrder> filterOrders(ReportFilterDto filter);
    List<ManufacturingOrder> getCustomerOrders(Long customerId);
    List<ManufacturingOrder> getDelayedOrders();
    long countOrders();
    long countOrdersByStatus(OrderStatus status);
    long countOrdersByPriority(Priority priority);
    long countDelayedOrders();

    OrderResponseDto convertToDto(ManufacturingOrder order);
}
