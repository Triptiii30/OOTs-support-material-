package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.ProductDto;
import com.smart.manufacturing.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getActiveProducts();
    Product getProductById(Long id);
    Product getProductByCode(String code);
    Product createProduct(ProductDto dto);
    Product updateProduct(Long id, ProductDto dto);
    void deleteProduct(Long id);
    List<Product> searchProducts(String query);
    List<Product> getProductsByCategory(String category);
    List<String> getAllCategories();
    long countProducts();
    ProductDto convertToDto(Product product);
}
