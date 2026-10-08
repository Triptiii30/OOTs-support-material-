package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.ProductionTaskDto;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionSchedule;
import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.enums.ProductionTaskStatus;

import java.util.List;
import java.util.PriorityQueue;

public interface SchedulingService {

    /**
     * Schedules an approved order by generating production tasks and assigning schedules.
     */
    ProductionSchedule scheduleOrder(Long orderId, String username);

    /**
     * Builds and returns a prioritized queue of pending manufacturing orders using DSA PriorityQueue.
     */
    PriorityQueue<ManufacturingOrder> buildPrioritizedProductionQueue();

    /**
     * Returns the prioritized production queue as a sorted list for UI display.
     */
    List<ManufacturingOrder> getPrioritizedProductionQueueList();

    List<ProductionSchedule> getAllSchedules();

    ProductionSchedule getScheduleById(Long id);
}
