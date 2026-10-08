package com.smart.manufacturing.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * StringProcessingUtil — Demonstrates String, StringBuilder, StringBuffer, StringTokenizer.
 */
public final class StringProcessingUtil {

    private StringProcessingUtil() {}

    // StringBuilder — single-threaded, mutable, fast
    public static String buildOrderSummary(String orderNumber, String customer,
                                           String status, String priority, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER SUMMARY ===\n");
        sb.append("Order   : ").append(orderNumber).append("\n");
        sb.append("Customer: ").append(customer).append("\n");
        sb.append("Status  : ").append(status).append("\n");
        sb.append("Priority: ").append(priority).append("\n");
        sb.append("Total   : INR ").append(String.format("%.2f", total)).append("\n");
        sb.append("====================");
        return sb.toString();
    }

    // StringBuffer — thread-safe (synchronized internally)
    public static synchronized String appendToLog(StringBuffer logBuffer, String entry) {
        logBuffer.append("[").append(java.time.LocalDateTime.now()).append("] ").append(entry).append("\n");
        return logBuffer.toString();
    }

    // StringTokenizer — legacy CSV parsing (academic demonstration)
    public static List<String> tokenizeCsvLine(String csvLine) {
        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(csvLine, ",");
        while (tokenizer.hasMoreTokens()) tokens.add(tokenizer.nextToken().trim());
        return tokens;
    }

    // String immutability demonstration
    public static String processOrderId(String raw) {
        String result = raw;          // original — immutable
        result = result.trim();       // new String object
        result = result.toUpperCase(); // new String object
        return result;                // raw is unchanged
    }

    // Method overloading
    public static String formatCurrency(double amount) { return formatCurrency(amount, "INR"); }
    public static String formatCurrency(double amount, String currency) {
        return currency + " " + String.format("%,.2f", amount);
    }

    public static boolean isValidOrderNumber(String s) {
        return s != null && !s.isEmpty() && s.startsWith("ORD-") && s.length() > 4;
    }
}
