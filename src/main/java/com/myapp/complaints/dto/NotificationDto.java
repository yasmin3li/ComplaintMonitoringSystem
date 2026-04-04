package com.myapp.complaints.dto;


import java.time.LocalDateTime;

public record NotificationDto(
        long id,
        String title,
        String message,
        LocalDateTime dateTime,
        NotificationReceiverDto notificationReceiverDto
) {
}
