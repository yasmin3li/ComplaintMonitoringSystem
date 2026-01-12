package com.myapp.complaints.projection;

import com.myapp.complaints.entity.Address;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.Institution;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

@Projection(name = "complaintEmployeeDetail", types = Complaint.class)
public interface ComplaintEmployeeDetailProjection {

    Long getId();
    String getTitle();
    String getDescription();
    ComplaintState getState();

    LocalDateTime getDateTimeOfAdd();
    LocalDateTime getDateTimeOfSolve();

    Institution getInstitution();
    Address getAddress();

    @Value("#{target.addedBy.userName}")
    String getCitizenName();

    @Value("#{target.addedBy.phoneNumber}")
    Long getCitizenPhone();

    @Value("#{target.addedBy.email}")
    String getCitizenEmail();
}
