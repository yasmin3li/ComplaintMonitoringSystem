package com.myapp.complaints.dto;

import com.myapp.complaints.enums.ComplaintPriority;

public record ComplaintPriorityDto(
        long complaintId,
        ComplaintPriority priority
) {
}
