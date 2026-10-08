package com.smart.manufacturing.repository;

import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.enums.ProductionTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionTaskRepository extends JpaRepository<ProductionTask, Long> {
    Optional<ProductionTask> findByTaskCode(String taskCode);
    List<ProductionTask> findByOrderId(Long orderId);
    List<ProductionTask> findByStatus(ProductionTaskStatus status);
    List<ProductionTask> findByScheduleId(Long scheduleId);

    @Query("SELECT t FROM ProductionTask t WHERE t.status != com.smart.manufacturing.enums.ProductionTaskStatus.COMPLETED " +
           "AND t.plannedEndDate < :now")
    List<ProductionTask> findDelayedTasks(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM ProductionTask t WHERE t.status != com.smart.manufacturing.enums.ProductionTaskStatus.COMPLETED " +
           "AND t.plannedEndDate < :now")
    long countDelayedTasks(@Param("now") LocalDateTime now);

    long countByStatus(ProductionTaskStatus status);

    List<ProductionTask> findAllByOrderByPlannedStartDateAsc();
}
