package com.smart.manufacturing.controller;

import com.smart.manufacturing.dto.OrderItemDto;
import com.smart.manufacturing.dto.OrderRequestDto;
import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.OrderStatusHistory;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.repository.OrderStatusHistoryRepository;
import com.smart.manufacturing.service.CustomerService;
import com.smart.manufacturing.service.OrderService;
import com.smart.manufacturing.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderStatusHistoryRepository statusHistoryRepository;

    @Autowired
    public OrderController(OrderService orderService,
                           CustomerService customerService,
                           ProductService productService,
                           OrderStatusHistoryRepository statusHistoryRepository) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.productService = productService;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @GetMapping
    public String listOrders(ReportFilterDto filter, Model model) {
        List<ManufacturingOrder> orders = orderService.filterOrders(filter);
        List<OrderResponseDto> dtos = orders.stream()
                .map(orderService::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("orders", dtos);
        model.addAttribute("filter", filter != null ? filter : new ReportFilterDto());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("delayedCount", orderService.countDelayedOrders());
        return "orders/list";
    }

    @GetMapping("/new")
    public String newOrderForm(Model model) {
        OrderRequestDto dto = new OrderRequestDto();
        dto.setRequiredDeliveryDate(LocalDate.now().plusDays(7));
        dto.setPriority(Priority.NORMAL);

        // Add 1 default empty item row
        dto.getItems().add(new OrderItemDto());

        model.addAttribute("orderDto", dto);
        model.addAttribute("customers", customerService.getActiveCustomers());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("priorities", Priority.values());
        return "orders/form";
    }

    @PostMapping("/save")
    public String saveOrder(@Valid @ModelAttribute("orderDto") OrderRequestDto dto,
                            BindingResult result,
                            Authentication authentication,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        // Remove empty item rows if any
        if (dto.getItems() != null) {
            dto.setItems(dto.getItems().stream()
                    .filter(item -> item.getProductId() != null && item.getQuantity() != null && item.getQuantity() > 0)
                    .collect(Collectors.toList()));
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            result.rejectValue("items", "NotEmpty", "Order must contain at least one valid product item");
        }

        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("priorities", Priority.values());
            return "orders/form";
        }

        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            ManufacturingOrder created = orderService.createOrder(dto, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order " + created.getOrderNumber() + " created successfully!");
            return "redirect:/orders/" + created.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("priorities", Priority.values());
            return "orders/form";
        }
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        ManufacturingOrder order = orderService.getOrderById(id);
        List<OrderStatusHistory> history = statusHistoryRepository.findByOrderIdOrderByChangedAtDesc(id);

        model.addAttribute("order", orderService.convertToDto(order));
        model.addAttribute("rawOrder", order);
        model.addAttribute("statusHistory", history);
        return "orders/view";
    }

    @PostMapping("/{id}/validate")
    public String validateOrder(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            orderService.validateOrder(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Order validated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/approve")
    public String approveOrder(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            orderService.approveOrder(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Order approved and stock reserved!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/schedule")
    public String scheduleOrder(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            orderService.scheduleOrder(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Order scheduled and production tasks generated!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/start-production")
    public String startProduction(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            orderService.startProduction(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Production commenced on shop floor!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/complete")
    public String completeOrder(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            orderService.completeOrder(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Order completed and inventory fulfilled!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @RequestParam(value = "reason", required = false) String reason,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String username = (authentication != null) ? authentication.getName() : "Web User";
        try {
            orderService.cancelOrder(id, reason, username);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled and allocated inventory released.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
