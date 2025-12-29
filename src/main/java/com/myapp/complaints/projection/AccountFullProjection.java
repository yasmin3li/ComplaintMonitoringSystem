package com.myapp.complaints.projection;

import com.myapp.complaints.entity.Citizen;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.entity.Account;
//import lombok.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;


//GET /accounts?projection=accountFullView
@Projection(name = "accountFullView", types = Account.class)
public interface AccountFullProjection {

    Long getId();
    String getUserName();
    String getEmail();
    AccountStatus getStatus();
    Long getPhoneNumber();
    String getNationalNumber();
    String getProfileImageUrl();

//    String getPassWord();
    @Value("#{target.role.name}")
    String getRoleName();

    // appear only if exist
    Citizen getCitizen();
    Employee getEmployee();
}


