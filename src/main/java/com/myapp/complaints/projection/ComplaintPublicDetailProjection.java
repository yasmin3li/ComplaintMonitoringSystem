package com.myapp.complaints.projection;

import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

@Projection(name = "complaintPublicDetail", types = Complaint.class)
public interface ComplaintPublicDetailProjection {

    Long getId();
    String getTitle();
    String getDescription();
    ComplaintState getState();

    LocalDateTime getDateTimeOfAdd();
    LocalDateTime getDateTimeOfSolve();

    Institution getInstitution();
    Sector getSector();
    Governorate getGovernorate();
    Address getAddress();
}
