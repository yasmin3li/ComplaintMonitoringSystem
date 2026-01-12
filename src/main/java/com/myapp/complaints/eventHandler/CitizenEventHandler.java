package com.myapp.complaints.eventHandler;

import com.myapp.complaints.entity.Citizen;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler(Citizen.class)
public class CitizenEventHandler {

    @HandleBeforeCreate
    public void beforeCreate(Citizen citizen) {

        if (citizen.getAccount() == null) {
            throw new RuntimeException("Citizen must be linked to an account");
        }
    }
}
