package com.smart.manufacturing.controller;

import com.smart.manufacturing.dto.CustomerDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.service.CustomerService;
import com.smart.manufacturing.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    @Autowired
    public CustomerController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "query", required = false) String query, Model model) {
        List<Customer> customers = (query != null && !query.isBlank()) ?
                customerService.searchCustomers(query) : customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        model.addAttribute("query", query);
        return "customers/list";
    }

    @GetMapping("/new")
    public String newCustomerForm(Model model) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerCode("CUST-" + (customerService.countCustomers() + 1001));
        model.addAttribute("customerDto", dto);
        model.addAttribute("isNew", true);
        return "customers/form";
    }

    @PostMapping("/save")
    public String saveCustomer(@Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("isNew", customerDto.getId() == null);
            return "customers/form";
        }

        try {
            if (customerDto.getId() == null) {
                customerService.createCustomer(customerDto);
                redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully!");
            } else {
                customerService.updateCustomer(customerDto.getId(), customerDto);
                redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully!");
            }
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isNew", customerDto.getId() == null);
            return "customers/form";
        }

        return "redirect:/customers";
    }

    @GetMapping("/edit/{id}")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setCustomerCode(customer.getCustomerCode());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setCompany(customer.getCompany());
        dto.setAddress(customer.getAddress());
        dto.setActive(customer.isActive());

        model.addAttribute("customerDto", dto);
        model.addAttribute("isNew", false);
        return "customers/form";
    }

    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("orders", orderService.getCustomerOrders(id));
        return "customers/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.deleteCustomer(id);
        redirectAttributes.addFlashAttribute("successMessage", "Customer status updated to inactive.");
        return "redirect:/customers";
    }
}
