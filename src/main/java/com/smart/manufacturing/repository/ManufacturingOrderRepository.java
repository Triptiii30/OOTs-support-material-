package com.smart.manufacturing.repository;

import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {

    Optional<ManufacturingOrder> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);

    List<ManufacturingOrder> findByCustomerIdOrderByOrderDateDesc(Long customerId);
    List<ManufacturingOrder> findByStatus(OrderStatus status);
    List<ManufacturingOrder> findByPriority(Priority priority);

    long countByStatus(OrderStatus status);
    long countByPriority(Priority priority);

    @Query("SELECT COUNT(o) FROM ManufacturingOrder o WHERE o.priority IN (com.smart.manufacturing.enums.Priority.HIGH, com.smart.manufacturing.enums.Priority.URGENT)")
    long countHighAndUrgentOrders();

    @Query("SELECT o FROM ManufacturingOrder o WHERE o.status NOT IN (com.smart.manufacturing.enums.OrderStatus.COMPLETED, com.smart.manufacturing.enums.OrderStatus.CANCELLED) " +
           "AND o.requiredDeliveryDate < :today")
    List<ManufacturingOrder> findDelayedOrders(@Param("today") LocalDate today);

    @Query("SELECT COUNT(o) FROM ManufacturingOrder o WHERE o.status NOT IN (com.smart.manufacturing.enums.OrderStatus.COMPLETED, com.smart.manufacturing.enums.OrderStatus.CANCELLED) " +
           "AND o.requiredDeliveryDate < :today")
    long countDelayedOrders(@Param("today") LocalDate today);

    @Query("SELECT o FROM ManufacturingOrder o WHERE " +
           "(:query IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(o.customer.company) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:priority IS NULL OR o.priority = :priority) AND " +
           "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderDate <= :endDate) " +
           "ORDER BY o.id DESC")
    List<ManufacturingOrder> filterOrders(@Param("query") String query,
                                          @Param("status") OrderStatus status,
                                          @Param("priority") Priority priority,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    List<ManufacturingOrder> findTop10ByOrderByIdDesc();
}
