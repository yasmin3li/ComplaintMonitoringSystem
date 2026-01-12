package com.myapp.complaints.projection;


import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.Institution;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

@Projection(name = "complaintSummary", types = Complaint.class)
public interface ComplaintSummaryProjection {

    Long getId();
    String getTitle();
    ComplaintState getState();
    LocalDateTime getDateTimeOfAdd();

    Institution getInstitution();
}

