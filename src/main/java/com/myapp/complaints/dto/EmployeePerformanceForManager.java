package com.myapp.complaints.dto;

import java.util.List;

public record EmployeePerformanceForManager(
        EmployeePerformanceDto employeePerformanceResponseDto,
        List<DelayedComplaintDto> delayedComplaints
) {
}
