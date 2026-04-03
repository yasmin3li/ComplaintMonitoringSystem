package com.myapp.complaints.mapper;

import com.myapp.complaints.DAO.NotificationReceiverRepo;
import com.myapp.complaints.dto.NotificationDto;
import com.myapp.complaints.dto.NotificationReceiverDto;
import com.myapp.complaints.entity.Notification;
import com.myapp.complaints.entity.NotificationReceiver;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final NotificationReceiverRepo notificationReceiverRepo;
    private final NotificationReceiverMapper notificationReceiverMapper;

    public NotificationDto toDto(Notification notification){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        //if the notification was sent to more than one account
        Optional<NotificationReceiver> notificationReceiver =
                notificationReceiverRepo.findByAccount_EmailAndNotification_Id(email,notification.getId());

        if(notificationReceiver.isPresent()){

            return new NotificationDto(
                    notification.getId(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notificationReceiverMapper.toDto(notificationReceiver.get())
            );
        }
        else {
            throw new ApiException("notification "+notification.getTitle()+" is not exist", HttpStatus.NOT_FOUND);

        }
    }
}
