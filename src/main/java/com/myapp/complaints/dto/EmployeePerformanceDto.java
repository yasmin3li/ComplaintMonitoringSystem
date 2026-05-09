package com.myapp.complaints.dto;

//@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeePerformanceDto(
        long employeeAccountId,
        long createdCount,
        long assignedCount,
        double responseRate,
        double score,
        String performanceLabel,
        String responseLabel,
        String badge,
        double normalizedHandled
) {
}

