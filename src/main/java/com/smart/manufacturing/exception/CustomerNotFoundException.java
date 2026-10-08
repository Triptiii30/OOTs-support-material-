package com.smart.manufacturing.exception;

/**
 * CustomerNotFoundException — Dedicated exception (extends ResourceNotFoundException).
 * Demonstrates: Exception hierarchy, multiple constructors, super keyword.
 */
public class CustomerNotFoundException extends ResourceNotFoundException {

    public CustomerNotFoundException(Long id) {
        super("Customer", "id", id.toString());
    }

    public CustomerNotFoundException(String identifier) {
        super("Customer not found: " + identifier);
    }

    // Method overloading in exceptions
    public CustomerNotFoundException(Long id, String context) {
        super("Customer " + id + " not found in context: " + context);
    }
}
