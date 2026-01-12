package com.myapp.complaints.eventHandler;

import com.myapp.complaints.entity.Employee;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler(Employee.class)
public class EmployeeEventHandler {

    @HandleBeforeCreate
    public void beforeCreate(Employee employee) {

        if (employee.getAccount() == null) {
            throw new RuntimeException("Employee must be linked to an account");
        }

        if (employee.getInstitution() == null) {
            throw new RuntimeException("Employee must belong to an institution");
        }
    }
}

