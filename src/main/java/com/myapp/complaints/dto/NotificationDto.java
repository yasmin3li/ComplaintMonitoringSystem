package com.myapp.complaints.dto;


public record NotificationDto(
        long id,
        String title,
        String message,
        NotificationReceiverDto notificationReceiverDto
) {
}
