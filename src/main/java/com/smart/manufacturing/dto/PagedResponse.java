package com.smart.manufacturing.dto;

import java.util.List;

/**
 * PagedResponse<T> — Generic paginated response.
 * Demonstrates: Generic class, bounded wildcard (? extends N).
 */
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / Math.max(1, size));
    }

    // Lower-bounded wildcard demo
    public static <N extends Number> double sumValues(List<? extends N> values) {
        return values.stream().mapToDouble(Number::doubleValue).sum();
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean hasNext() { return page < totalPages - 1; }
    public boolean hasPrevious() { return page > 0; }
}
