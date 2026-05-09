package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.EmployeePerformanceDto;
import com.myapp.complaints.entity.EmployeePerformanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmployeePerformanceSnapshotRepo extends JpaRepository<EmployeePerformanceSnapshot, Long> {
    List<EmployeePerformanceDto> findByEmployeeAccountIdOrderByPeriodStartDesc(Long id);

    Optional<EmployeePerformanceSnapshot> findByEmployeeAccountIdAndPeriodStartAndPeriodEnd(Long accountId, LocalDateTime start, LocalDateTime end);

    List<EmployeePerformanceSnapshot>
    findByEmployeeAccountIdOrderByComputedAtDesc(Long accountId);

}