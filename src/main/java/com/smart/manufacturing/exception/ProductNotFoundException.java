package com.smart.manufacturing.exception;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
