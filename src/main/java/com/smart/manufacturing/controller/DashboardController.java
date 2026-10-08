package com.smart.manufacturing.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.manufacturing.dto.DashboardStatsDto;
import com.smart.manufacturing.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DashboardController(ReportService reportService) {
        this.reportService = reportService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = reportService.getDashboardStats();
        model.addAttribute("stats", stats);

        // Serialize chart data to JSON for Chart.js
        try {
            model.addAttribute("statusChartJson", objectMapper.writeValueAsString(stats.getOrdersByStatus()));
            model.addAttribute("priorityChartJson", objectMapper.writeValueAsString(stats.getOrdersByPriority()));
            model.addAttribute("categoryChartJson", objectMapper.writeValueAsString(stats.getInventoryByCategory()));
        } catch (JsonProcessingException e) {
            model.addAttribute("statusChartJson", "{}");
            model.addAttribute("priorityChartJson", "{}");
            model.addAttribute("categoryChartJson", "{}");
        }

        return "dashboard";
    }
}
