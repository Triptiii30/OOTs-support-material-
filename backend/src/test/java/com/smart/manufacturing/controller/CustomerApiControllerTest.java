package com.smart.manufacturing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.manufacturing.controller.api.CustomerApiController;
import com.smart.manufacturing.dto.CustomerDto;
import com.smart.manufacturing.entity.Customer;
import com.smart.manufacturing.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerApiController.class)
@AutoConfigureMockMvc
@DisplayName("Customer API Controller MockMvc Tests")
class CustomerApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "staff", authorities = {"ROLE_ORDER_STAFF"})
    @DisplayName("GET /api/customers returns customer list")
    void testGetCustomersList() throws Exception {
        Customer c = new Customer("C-1", "Alpha Tech", "alpha@tech.com", "+123", "Alpha", "Plant 1");
        c.setId(1L);

        when(customerService.getAllCustomers()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].customerCode").value("C-1"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("POST /api/customers creates new customer")
    void testCreateCustomer() throws Exception {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerCode("CUST-999");
        dto.setName("Beta Industrial");
        dto.setEmail("beta@industrial.com");
        dto.setPhone("+91 9999");
        dto.setCompany("Beta Group");

        Customer created = new Customer(dto.getCustomerCode(), dto.getName(), dto.getEmail(), dto.getPhone(), dto.getCompany(), "");
        created.setId(99L);

        when(customerService.createCustomer(any())).thenReturn(created);

        mockMvc.perform(post("/api/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerCode").value("CUST-999"));
    }
}
