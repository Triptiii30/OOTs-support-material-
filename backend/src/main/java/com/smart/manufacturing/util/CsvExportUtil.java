package com.smart.manufacturing.util;

import com.smart.manufacturing.dto.OrderResponseDto;
import com.smart.manufacturing.dto.ProductDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.ProductionTask;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Utility for CSV generation demonstrating Java I/O and File Handling concepts.
 */
public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static String exportOrdersToCsv(List<ManufacturingOrder> orders) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // CSV Header
        pw.println("Order Number,Customer Name,Company,Order Date,Delivery Deadline,Priority,Status,Total Items,Grand Total,Delayed");

        for (ManufacturingOrder o : orders) {
            pw.printf("%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%b%n",
                    escapeSpecialCharacters(o.getOrderNumber()),
                    escapeSpecialCharacters(o.getCustomer() != null ? o.getCustomer().getName() : "N/A"),
                    escapeSpecialCharacters(o.getCustomer() != null ? o.getCustomer().getCompany() : "N/A"),
                    o.getOrderDate(),
                    o.getRequiredDeliveryDate(),
                    o.getPriority(),
                    o.getStatus(),
                    o.getTotalQuantity(),
                    o.getGrandTotal(),
                    o.isDelayed()
            );
        }
        pw.flush();
        return sw.toString();
    }

    public static String exportInventoryToCsv(List<Inventory> inventories) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Product Code,Product Name,Category,Current Stock,Allocated Stock,Available Stock,Min Stock Level,Status,Unit Price");

        for (Inventory inv : inventories) {
            pw.printf("%s,%s,%s,%d,%d,%d,%d,%s,%.2f%n",
                    escapeSpecialCharacters(inv.getProduct().getProductCode()),
                    escapeSpecialCharacters(inv.getProduct().getName()),
                    escapeSpecialCharacters(inv.getProduct().getCategory()),
                    inv.getCurrentStock(),
                    inv.getAllocatedStock(),
                    inv.getAvailableStock(),
                    inv.getMinStockLevel(),
                    inv.isLowStock() ? "LOW_STOCK" : "NORMAL",
                    inv.getProduct().getUnitPrice()
            );
        }
        pw.flush();
        return sw.toString();
    }

    public static String exportProductionTasksToCsv(List<ProductionTask> tasks) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Task Code,Order Number,Product Name,Quantity,Assigned Team,Priority,Status,Progress %,Start Date,End Date,Delayed");

        for (ProductionTask t : tasks) {
            pw.printf("%s,%s,%s,%d,%s,%s,%s,%d%%,%s,%s,%b%n",
                    escapeSpecialCharacters(t.getTaskCode()),
                    escapeSpecialCharacters(t.getOrder().getOrderNumber()),
                    escapeSpecialCharacters(t.getProduct().getName()),
                    t.getQuantity(),
                    escapeSpecialCharacters(t.getAssignedTeam()),
                    t.getPriority(),
                    t.getStatus(),
                    t.getProgressPercentage(),
                    t.getPlannedStartDate(),
                    t.getPlannedEndDate(),
                    t.isDelayed()
            );
        }
        pw.flush();
        return sw.toString();
    }

    public static String exportCustomersToCsv(List<Customer> customers) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Customer Code,Name,Company,Email,Phone,Address,Active,Orders Count");

        for (Customer c : customers) {
            pw.printf("%s,%s,%s,%s,%s,%s,%b,%d%n",
                    escapeSpecialCharacters(c.getCustomerCode()),
                    escapeSpecialCharacters(c.getName()),
                    escapeSpecialCharacters(c.getCompany()),
                    escapeSpecialCharacters(c.getEmail()),
                    escapeSpecialCharacters(c.getPhone()),
                    escapeSpecialCharacters(c.getAddress()),
                    c.isActive(),
                    c.getOrders().size()
            );
        }
        pw.flush();
        return sw.toString();
    }
}
