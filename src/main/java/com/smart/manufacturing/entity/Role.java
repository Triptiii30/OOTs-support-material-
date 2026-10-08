package com.smart.manufacturing.entity;

import com.smart.manufacturing.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private UserRole name;

    @Column(name = "description", length = 200)
    private String description;

    public Role() {
    }

    public Role(UserRole name, String description) {
        this.name = name;
        this.description = description;
    }

    public UserRole getName() {
        return name;
    }

    public void setName(UserRole name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
