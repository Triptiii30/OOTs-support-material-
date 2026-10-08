package com.smart.manufacturing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Customer entity for clients placing manufacturing orders.
 * Demonstrates OOP Encapsulation and relationship management.
 */
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Column(name = "customer_code", unique = true, nullable = false, length = 30)
    private String customerCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ManufacturingOrder> orders = new ArrayList<>();

    public Customer() {
    }

    public Customer(String customerCode, String name, String email, String phone, String company, String address) {
        this.customerCode = customerCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.address = address;
        this.active = true;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ManufacturingOrder> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public void addOrder(ManufacturingOrder order) {
        if (order != null) {
            this.orders.add(order);
            order.setCustomer(this);
        }
    }
}
