package com.smart.manufacturing.service;

import com.smart.manufacturing.dto.CustomerDto;
import com.smart.manufacturing.entity.Customer;

import java.util.List;

public interface CustomerService {
    List<Customer> getAllCustomers();
    List<Customer> getActiveCustomers();
    Customer getCustomerById(Long id);
    Customer getCustomerByCode(String code);
    Customer createCustomer(CustomerDto dto);
    Customer updateCustomer(Long id, CustomerDto dto);
    void deleteCustomer(Long id);
    List<Customer> searchCustomers(String query);
    long countCustomers();
}
