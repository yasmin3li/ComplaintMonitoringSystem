package com.myapp.complaints.dto;

import java.util.List;

public record SpecifiedEmployeePerformanceResponseDto(
        List<EmployeeBadgeDto> badges

) {
}
