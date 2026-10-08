package com.smart.manufacturing.service.impl;

import com.smart.manufacturing.dto.ProductionTaskDto;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.ProductionTaskStatus;
import com.smart.manufacturing.exception.ResourceNotFoundException;
import com.smart.manufacturing.repository.ManufacturingOrderRepository;
import com.smart.manufacturing.repository.ProductionTaskRepository;
import com.smart.manufacturing.service.ProductionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProductionServiceImpl implements ProductionService {

    private static final Logger log = LoggerFactory.getLogger(ProductionServiceImpl.class);

    private final ProductionTaskRepository taskRepository;
    private final ManufacturingOrderRepository orderRepository;

    @Autowired
    public ProductionServiceImpl(ProductionTaskRepository taskRepository,
                                 ManufacturingOrderRepository orderRepository) {
        this.taskRepository = taskRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionTask> getAllTasks() {
        return taskRepository.findAllByOrderByPlannedStartDateAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionTask getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production task not found with ID: " + id));
    }

    @Override
    public ProductionTask updateTaskProgress(Long taskId, int progress, ProductionTaskStatus status,
                                            String notes, String username) {
        log.info("Updating task ID: {} to progress: {}% with status: {} by user: {}", taskId, progress, status, username);
        ProductionTask task = getTaskById(taskId);
        task.updateProgress(progress, status);
        if (notes != null && !notes.isBlank()) {
            task.setNotes(notes);
        }

        ProductionTask saved = taskRepository.save(task);

        // Check related order status transitions
        ManufacturingOrder order = task.getOrder();
        if (order != null) {
            if (status == ProductionTaskStatus.IN_PROGRESS && order.getStatus() == OrderStatus.SCHEDULED) {
                order.setStatus(OrderStatus.IN_PROGRESS);
                orderRepository.save(order);
            }

            // Check if all tasks for this order are completed
            List<ProductionTask> orderTasks = taskRepository.findByOrderId(order.getId());
            boolean allCompleted = !orderTasks.isEmpty() &&
                    orderTasks.stream().allMatch(t -> t.getStatus() == ProductionTaskStatus.COMPLETED);

            if (allCompleted && order.getStatus() == OrderStatus.IN_PROGRESS) {
                log.info("All tasks completed for order {}. Order ready for completion.", order.getOrderNumber());
            }
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionTask> getTasksByOrderId(Long orderId) {
        return taskRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionTask> getDelayedTasks() {
        return taskRepository.findDelayedTasks(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTasksByStatus(ProductionTaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDelayedTasks() {
        return taskRepository.countDelayedTasks(LocalDateTime.now());
    }

    @Override
    public ProductionTaskDto convertToDto(ProductionTask task) {
        ProductionTaskDto dto = new ProductionTaskDto();
        dto.setId(task.getId());
        dto.setTaskCode(task.getTaskCode());
        if (task.getOrder() != null) {
            dto.setOrderId(task.getOrder().getId());
            dto.setOrderNumber(task.getOrder().getOrderNumber());
        }
        if (task.getProduct() != null) {
            dto.setProductId(task.getProduct().getId());
            dto.setProductName(task.getProduct().getName());
        }
        dto.setQuantity(task.getQuantity());
        dto.setAssignedTeam(task.getAssignedTeam());
        dto.setPriority(task.getPriority());
        dto.setPlannedStartDate(task.getPlannedStartDate());
        dto.setPlannedEndDate(task.getPlannedEndDate());
        dto.setActualCompletionDate(task.getActualCompletionDate());
        dto.setStatus(task.getStatus());
        dto.setProgressPercentage(task.getProgressPercentage());
        dto.setNotes(task.getNotes());
        dto.setDelayed(task.isDelayed());
        return dto;
    }
}
