package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.ProductionTaskDto;
import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.enums.ProductionTaskStatus;

import java.util.List;

public interface ProductionService {
    List<ProductionTask> getAllTasks();
    ProductionTask getTaskById(Long id);
    ProductionTask updateTaskProgress(Long taskId, int progress, ProductionTaskStatus status, String notes, String username);
    List<ProductionTask> getTasksByOrderId(Long orderId);
    List<ProductionTask> getDelayedTasks();
    long countTasksByStatus(ProductionTaskStatus status);
    long countDelayedTasks();
    ProductionTaskDto convertToDto(ProductionTask task);
}
