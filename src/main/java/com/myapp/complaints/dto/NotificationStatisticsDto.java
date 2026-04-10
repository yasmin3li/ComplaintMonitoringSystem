package com.myapp.complaints.dto;

public record NotificationStatisticsDto(
        long total,
        long notRead
) {
}
