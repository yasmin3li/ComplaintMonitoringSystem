package com.myapp.complaints.dto;

import com.myapp.complaints.entity.Employee;

import java.util.List;

public record AssignComplaintDto(
        long complaintId,
        long assignedTo
) {
}
