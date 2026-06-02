package com.myapp.complaints.dto;

import java.util.List;

public record EmployeePerformanceForManager(
        List<DelayedComplaintDto> delayedComplaints,
        EmployeePerformanceDto employeePerformanceResponseDto
) {
}
