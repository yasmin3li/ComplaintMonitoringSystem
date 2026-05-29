package com.myapp.complaints.dto;

import java.util.List;

public record ManagerEmployeeRecommendationDto(

        Long employeeId,
        String employeeName,
        long assignedTasks,
        long inProgressTasks,
        long resolvedComplaints,
        List<DelayedComplaintDto> delayedComplaints,
        LoadTagDto employeeBadgeDto


//        double averageResponseDays,
//        double score,
//        List<EmployeeBadgeDto> badges

) {
}
