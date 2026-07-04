package com.myapp.complaints.dto;

public record TechnicDashBoardStatisticsDto(
        long AssignedComplaints,
        long InProgressComplaints,
        long Delay,
        long Solved,
        long complaintsCount
) {
}