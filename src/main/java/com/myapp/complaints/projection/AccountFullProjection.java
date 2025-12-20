package com.myapp.complaints.projection;

import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.entity.Account;
//import lombok.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;


///accounts?projection=accountView
@Projection(name = "accountView", types = Account.class)
public interface AccountProjection {

    Long getId();
    String getUserName();
    String getEmail();
    String getProfileImageUrl();
    AccountStatus getStatus();

    @Value("#{target.role.name}")
    String getRoleName();
}

