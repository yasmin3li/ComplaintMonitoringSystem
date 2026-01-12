package com.myapp.complaints.projection;

import com.myapp.complaints.entity.Address;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.entity.Institution;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;
import java.util.List;

@Projection(name = "complaintOwnerDetail", types = Complaint.class)
public interface ComplaintOwnerDetailProjection {

    Long getId();
    String getTitle();
    String getDescription();
    ComplaintState getState();

    LocalDateTime getDateTimeOfAdd();
    LocalDateTime getDateTimeOfSolve();

    Institution getInstitution();
    Address getAddress();

    List<ComplaintTrackingLog> getLogs();
}

/*
GET /complaints?projection=complaintSummary
GET /complaints/5?projection=complaintPublicDetail
GET /complaints/5?projection=complaintEmployeeDetail
GET /complaints/5?projection=complaintOwnerDetail

 */