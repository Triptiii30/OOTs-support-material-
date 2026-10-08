package com.smart.manufacturing.service.impl;

import com.smart.manufacturing.dto.ProductDto;
import com.smart.manufacturing.entity.Inventory;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.enums.TransactionType;
import com.smart.manufacturing.exception.DuplicateResourceException;
import com.smart.manufacturing.exception.ProductNotFoundException;
import com.smart.manufacturing.repository.InventoryRepository;
import com.smart.manufacturing.repository.InventoryTransactionRepository;
import com.smart.manufacturing.repository.ProductRepository;
import com.smart.manufacturing.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductByCode(String code) {
        return productRepository.findByProductCode(code)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + code));
    }

    @Override
    public Product createProduct(ProductDto dto) {
        log.info("Creating product with SKU: {}", dto.getProductCode());
        if (productRepository.existsByProductCode(dto.getProductCode())) {
            throw new DuplicateResourceException("Product SKU '" + dto.getProductCode() + "' already exists");
        }

        Product product = new Product(
                dto.getProductCode(),
                dto.getName(),
                dto.getDescription(),
                dto.getCategory(),
                dto.getUnitPrice(),
                dto.getProductionDurationHours(),
                dto.getMinStockLevel()
        );
        product.setActive(dto.isActive());

        Product savedProduct = productRepository.save(product);

        // Maintain strict 1-to-1 relationship with inventory
        Inventory inventory = new Inventory(
                savedProduct,
                dto.getInitialStock() > 0 ? dto.getInitialStock() : 0,
                dto.getMinStockLevel()
        );
        inventoryRepository.save(inventory);
        savedProduct.setInventory(inventory);

        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, ProductDto dto) {
        log.info("Updating product ID: {}", id);
        Product product = getProductById(id);

        if (!product.getProductCode().equalsIgnoreCase(dto.getProductCode()) &&
                productRepository.existsByProductCode(dto.getProductCode())) {
            throw new DuplicateResourceException("Product SKU '" + dto.getProductCode() + "' is already in use");
        }

        product.setProductCode(dto.getProductCode());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setUnitPrice(dto.getUnitPrice());
        product.setProductionDurationHours(dto.getProductionDurationHours());
        product.setMinStockLevel(dto.getMinStockLevel());
        product.setActive(dto.isActive());

        if (product.getInventory() != null) {
            product.getInventory().setMinStockLevel(dto.getMinStockLevel());
        }

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deactivating product ID: {}", id);
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setProductCode(product.getProductCode());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setUnitPrice(product.getUnitPrice());
        dto.setProductionDurationHours(product.getProductionDurationHours());
        dto.setMinStockLevel(product.getMinStockLevel());
        dto.setActive(product.isActive());

        if (product.getInventory() != null) {
            dto.setCurrentStock(product.getInventory().getCurrentStock());
            dto.setAllocatedStock(product.getInventory().getAllocatedStock());
            dto.setAvailableStock(product.getInventory().getAvailableStock());
            dto.setLowStock(product.getInventory().isLowStock());
        }
        return dto;
    }
}
