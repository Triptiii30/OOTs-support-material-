package com.smart.manufacturing.repository;

import com.smart.manufacturing.entity.ProductionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionScheduleRepository extends JpaRepository<ProductionSchedule, Long> {
    Optional<ProductionSchedule> findByScheduleCode(String scheduleCode);
    List<ProductionSchedule> findByScheduleDate(LocalDate scheduleDate);
    List<ProductionSchedule> findAllByOrderByScheduleDateDesc();
}
