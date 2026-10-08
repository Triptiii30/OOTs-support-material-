package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.DashboardStatsDto;
import com.smart.manufacturing.dto.ReportFilterDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionTask;

import java.util.List;

public interface ReportService {
    DashboardStatsDto getDashboardStats();
    List<ManufacturingOrder> getFilteredOrdersReport(ReportFilterDto filter);
    List<ProductionTask> getProductionReport();
    List<Inventory> getInventoryReport();
    List<Customer> getCustomerReport();

    // CSV Exports (File Handling)
    String exportOrdersCsv(ReportFilterDto filter);
    String exportInventoryCsv();
    String exportProductionCsv();
    String exportCustomersCsv();
}
