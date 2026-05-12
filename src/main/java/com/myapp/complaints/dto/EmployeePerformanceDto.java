package com.myapp.complaints.dto;

//@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeePerformanceDto(
        long employeeAccountId,
        long comingCount,
        long handledCount,
        double responseRate,
        double score,
        String performanceLabel,
        String responseLabel,
        String badge,
        double normalizedHandled
) {
}

