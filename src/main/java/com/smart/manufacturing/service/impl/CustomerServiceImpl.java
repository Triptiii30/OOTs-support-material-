package com.smart.manufacturing.service.impl;

import com.smart.manufacturing.dto.CustomerDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.exception.CustomerNotFoundException;
import com.smart.manufacturing.exception.DuplicateResourceException;
import com.smart.manufacturing.repository.CustomerRepository;
import com.smart.manufacturing.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByCode(String code) {
        return customerRepository.findByCustomerCode(code)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with code: " + code));
    }

    @Override
    public Customer createCustomer(CustomerDto dto) {
        log.info("Creating new customer with email: {}", dto.getEmail());
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Customer email '" + dto.getEmail() + "' already exists");
        }
        if (customerRepository.existsByCustomerCode(dto.getCustomerCode())) {
            throw new DuplicateResourceException("Customer code '" + dto.getCustomerCode() + "' already exists");
        }

        Customer customer = new Customer(
                dto.getCustomerCode(),
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getCompany(),
                dto.getAddress()
        );
        customer.setActive(dto.isActive());
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, CustomerDto dto) {
        log.info("Updating customer ID: {}", id);
        Customer customer = getCustomerById(id);

        if (!customer.getEmail().equalsIgnoreCase(dto.getEmail()) && customerRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Customer email '" + dto.getEmail() + "' is already in use by another customer");
        }

        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setCompany(dto.getCompany());
        customer.setAddress(dto.getAddress());
        customer.setActive(dto.isActive());
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        log.info("Deactivating customer ID: {}", id);
        Customer customer = getCustomerById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String query) {
        if (query == null || query.isBlank()) {
            return getAllCustomers();
        }
        return customerRepository.searchCustomers(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public long countCustomers() {
        return customerRepository.count();
    }
}
