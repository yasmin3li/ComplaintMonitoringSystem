package com.myapp.complaints.dto;

import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;

import java.time.LocalDateTime;

public record ComplaintTrackingLogDto(
        ComplaintState previousState,
        ActionType actionType,
        String comments,
        LocalDateTime actionDate,
        Employee assignedTo
) {
}
