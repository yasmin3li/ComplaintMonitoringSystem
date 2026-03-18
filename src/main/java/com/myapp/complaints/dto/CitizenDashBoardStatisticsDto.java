package com.myapp.complaints.dto;

public record CitizenDashBoardStatisticsDto(
    long totalComplaints,
    long inProgressComplaints,
     long solvedComplaints,
     String completionRate
     )
    {}
