package com.smart.manufacturing.controller;

import com.smart.manufacturing.dto.ProductDto;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "category", required = false) String category,
                               Model model) {
        List<Product> products;
        if (query != null && !query.isBlank()) {
            products = productService.searchProducts(query);
        } else if (category != null && !category.isBlank()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        List<ProductDto> dtos = products.stream()
                .map(productService::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("products", dtos);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("query", query);
        return "products/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        ProductDto dto = new ProductDto();
        dto.setProductCode("PRD-" + (productService.countProducts() + 101));
        model.addAttribute("productDto", dto);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("isNew", true);
        return "products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("productDto") ProductDto productDto,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isNew", productDto.getId() == null);
            return "products/form";
        }

        try {
            if (productDto.getId() == null) {
                productService.createProduct(productDto);
                redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
            } else {
                productService.updateProduct(productDto.getId(), productDto);
                redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            }
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isNew", productDto.getId() == null);
            return "products/form";
        }

        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        ProductDto dto = productService.convertToDto(product);
        model.addAttribute("productDto", dto);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("isNew", false);
        return "products/form";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", productService.convertToDto(product));
        return "products/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product status updated to inactive.");
        return "redirect:/products";
    }
}
