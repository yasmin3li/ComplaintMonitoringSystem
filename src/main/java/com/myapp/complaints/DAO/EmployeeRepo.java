package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.entity.Governorate;
import com.myapp.complaints.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee,Long> {

    @RestResource(exported = false)
    Optional<Employee> findByAccountId(Long accountId);

    List<Employee> findByGovernorate_IdAndInstitution_IdAndAccount_Role_Id(Long governorateId,Long institutionId, long i);

    Employee findByAccount_Email(String email);

}
