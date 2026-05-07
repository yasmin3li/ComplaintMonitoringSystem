package com.myapp.complaints.dto;

public record EmployeeDashBoardStatisticsDto(
        long NewComplaints,
        long InReviewComplaints,
        long ForwardedComplaints,
        long Rejected
) {
}
