package com.myapp.complaints.dto;

import com.myapp.complaints.enums.ComplaintPriority;
import com.myapp.complaints.enums.ComplaintState;

import java.time.LocalDateTime;

public record DelayedComplaintDto(

        Long employeeId,
        String email,
        String name,
        Long complaintId,
        String title,
        ComplaintPriority priority,
        LocalDateTime lastUpdate,
        ComplaintState state,
        Double daysNumber

) {}