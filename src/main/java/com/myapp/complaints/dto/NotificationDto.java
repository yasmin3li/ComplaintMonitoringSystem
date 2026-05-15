package com.myapp.complaints.dto;


import java.time.LocalDateTime;

public record NotificationDto(
        long id,
        long complaintId,
        String title,
        String message,
        LocalDateTime dateTime,
        NotificationReceiverDto notificationReceiverDto
) {
}
