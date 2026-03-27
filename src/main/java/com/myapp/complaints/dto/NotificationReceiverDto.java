package com.myapp.complaints.dto;

import java.time.LocalDateTime;

public record NotificationReceiverDto(
        boolean isRead,
        LocalDateTime dateTime
) {
}
