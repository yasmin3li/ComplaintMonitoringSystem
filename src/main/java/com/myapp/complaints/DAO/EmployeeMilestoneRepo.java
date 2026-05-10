package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.EmployeeMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeMilestoneRepo extends JpaRepository<EmployeeMilestone,Long> {
    Optional<EmployeeMilestone> findByEmployee_Id(Long id);
}
