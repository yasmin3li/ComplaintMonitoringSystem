package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee,Long> {

    @RestResource(exported = false)
    Optional<Employee> findByAccountId(Long accountId);
}
