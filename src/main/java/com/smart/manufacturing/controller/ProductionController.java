package com.smart.manufacturing.controller;

import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ProductionTaskDto;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionSchedule;
import com.smart.manufacturing.entity.ProductionTask;
import com.smart.manufacturing.enums.ProductionTaskStatus;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.ProductionService;
import com.smart.manufacturing.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductionController {

    private final ProductionService productionService;
    private final SchedulingService schedulingService;
    private final OrderService orderService;

    @Autowired
    public ProductionController(ProductionService productionService,
                                SchedulingService schedulingService,
                                OrderService orderService) {
        this.productionService = productionService;
        this.schedulingService = schedulingService;
        this.orderService = orderService;
    }

    @GetMapping("/production")
    public String productionTasks(Model model) {
        List<ProductionTask> tasks = productionService.getAllTasks();
        List<ProductionTaskDto> dtos = tasks.stream()
                .map(productionService::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("tasks", dtos);
        model.addAttribute("delayedCount", productionService.countDelayedTasks());
        model.addAttribute("taskStatuses", ProductionTaskStatus.values());
        return "production/tasks";
    }

    @PostMapping("/production/tasks/{id}/update")
    public String updateTaskProgress(@PathVariable Long id,
                                     @RequestParam int progress,
                                     @RequestParam ProductionTaskStatus status,
                                     @RequestParam(required = false) String notes,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            productionService.updateTaskProgress(id, progress, status, notes, username);
            redirectAttributes.addFlashAttribute("successMessage", "Task updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/production";
    }

    @GetMapping("/scheduling")
    public String schedulingQueue(Model model) {
        // DSA PriorityQueue evaluation
        List<ManufacturingOrder> prioritizedOrders = schedulingService.getPrioritizedProductionQueueList();
        List<OrderResponseDto> orderDtos = prioritizedOrders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());

        List<ProductionSchedule> schedules = schedulingService.getAllSchedules();

        model.addAttribute("prioritizedOrders", orderDtos);
        model.addAttribute("schedules", schedules);
        return "production/scheduling";
    }
}
