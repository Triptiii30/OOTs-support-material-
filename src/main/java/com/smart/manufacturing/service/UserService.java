package com.smart.manufacturing.service;

import com.smart.manufacturing.entity.User;
import com.smart.manufacturing.enums.UserRole;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByUsername(String username);
    User registerUser(String username, String rawPassword, String fullName, String email, String department, Set<UserRole> roles);
    boolean existsByUsername(String username);
    long countUsers();
}
