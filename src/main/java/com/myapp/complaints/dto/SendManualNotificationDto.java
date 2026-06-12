package com.myapp.complaints.dto;

public record SendManualNotificationDto(

        Long accountId,
        Long complaintId,
        String title,
        String message

) {}
