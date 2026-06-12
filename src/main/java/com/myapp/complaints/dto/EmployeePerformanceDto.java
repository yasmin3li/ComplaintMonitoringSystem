package com.myapp.complaints.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record EmployeePerformanceDto(

        long employeeAccountId,

        @JsonIgnore
        long comingCount,
        long assigned,
        long handledCount,
        double responseRate,
        double score,
//        double normalizedHandled,
        List<EmployeeBadgeDto> badges

) {
}
