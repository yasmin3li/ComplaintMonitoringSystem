package com.myapp.complaints.dto;

public record ReceptionistDashBoardStatisticsDto(
        long NewComplaints,
        long InReviewComplaints,
        long ForwardedComplaints,
        long Rejected
) {
}
