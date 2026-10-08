package com.smart.manufacturing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ProductionSchedule groups tasks planned for manufacturing time windows.
 */
@Entity
@Table(name = "production_schedules")
public class ProductionSchedule extends BaseEntity {

    @Column(name = "schedule_code", unique = true, nullable = false, length = 50)
    private String scheduleCode;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "notes", length = 500)
    private String notes;

    @JsonIgnore
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionTask> tasks = new ArrayList<>();

    public ProductionSchedule() {
    }

    public ProductionSchedule(String scheduleCode, LocalDate scheduleDate, String name, String notes) {
        this.scheduleCode = scheduleCode;
        this.scheduleDate = scheduleDate;
        this.name = name;
        this.notes = notes;
        this.status = "ACTIVE";
    }

    public String getScheduleCode() {
        return scheduleCode;
    }

    public void setScheduleCode(String scheduleCode) {
        this.scheduleCode = scheduleCode;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<ProductionTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(ProductionTask task) {
        if (task != null) {
            this.tasks.add(task);
            task.setSchedule(this);
        }
    }
}
