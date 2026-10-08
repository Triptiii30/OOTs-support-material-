package com.smart.manufacturing.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GenericResponse<T> — Reusable generic API response wrapper.
 * Demonstrates: Generic class, type parameters, bounded generics, factory methods.
 */
public class GenericResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private LocalDateTime timestamp;

    private GenericResponse() { this.timestamp = LocalDateTime.now(); }

    public static <T> GenericResponse<T> success(T data, String message) {
        GenericResponse<T> r = new GenericResponse<>();
        r.success = true; r.message = message; r.data = data;
        return r;
    }

    public static <T> GenericResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> GenericResponse<T> failure(String message) {
        GenericResponse<T> r = new GenericResponse<>();
        r.success = false; r.message = message;
        return r;
    }

    public static <T> GenericResponse<T> failure(String message, List<String> errors) {
        GenericResponse<T> r = failure(message);
        r.errors = errors;
        return r;
    }

    // Bounded generic method — only works with Number subtypes
    public static <N extends Number> GenericResponse<N> numericSuccess(N value, String label) {
        return success(value, label + ": " + value);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public List<String> getErrors() { return errors; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
