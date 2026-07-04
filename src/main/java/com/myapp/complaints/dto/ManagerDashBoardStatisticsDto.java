package com.myapp.complaints.dto;

public record ManagerDashBoardStatisticsDto(
        long Delay,
        long NotAssignedComplaints,
        long AssignedComplaints,
        long InProgressComplaints,
        long Solved,
        long employeesCount
) {
}