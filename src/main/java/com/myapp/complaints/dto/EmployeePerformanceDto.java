package com.myapp.complaints.dto;

import java.util.List;

public record EmployeePerformanceDto(

        long employeeAccountId,
        long comingCount,
        long handledCount,
        double responseRate,
        double score,
        double normalizedHandled,
        List<EmployeeBadgeDto> badges

) {
}
