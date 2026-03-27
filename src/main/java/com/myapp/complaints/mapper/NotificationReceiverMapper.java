package com.myapp.complaints.mapper;

import com.myapp.complaints.dto.NotificationReceiverDto;
import com.myapp.complaints.entity.Notification;
import com.myapp.complaints.entity.NotificationReceiver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationReceiverMapper {
    public NotificationReceiverDto toDto(NotificationReceiver notificationReceiver){

        return new NotificationReceiverDto(
                notificationReceiver.getIsRead(),
                notificationReceiver.getReadAt()
                );
    }
}
