package com.myapp.complaints.dto;

import com.myapp.complaints.enums.ComplaintPriority;
import com.myapp.complaints.enums.ComplaintState;

import java.time.LocalDateTime;

public interface DelayedComplaintsProjection {
        Long getComplaintId();
        String getTitle();
        ComplaintPriority getPriority();
        LocalDateTime getLastUpdate();
        ComplaintState getState();
}
