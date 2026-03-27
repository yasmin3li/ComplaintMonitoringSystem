package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.NotificationReceiverRepo;
import com.myapp.complaints.DAO.NotificationRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.NotificationDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.Notification;
import com.myapp.complaints.entity.NotificationReceiver;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.mapper.NotificationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationReceiverRepo notificationReceiverRepo;
    private final NotificationRepo notificationRepo;
    private final AccountRepo accountRepo;
    private final ComplaintRepo complaintRepo;


    @Transactional
    public Notification buildNotification(Complaint complaint, ComplaintState complaintState, String reason) {

        Complaint complaintVerify = complaintRepo.findByIdAndDeletedFalse(complaint.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        Notification notification = new Notification();
        notification.setTitle(NotificationFactory.getTitle(complaintState));
        notification.setMessage(NotificationFactory.getMessage(complaintState, complaint.getTitle(), reason));
        notification.setComplaint(complaint);
        notificationRepo.save(notification);
        return notification;
    }

    @Transactional
    public void sendNotification(Notification notification, Account account) {

        Account accountVerify = accountRepo.findByEmail(account.getEmail()).
                orElseThrow(()-> new ResourceNotFoundException("account not found"));

        NotificationReceiver notificationReceiver = new NotificationReceiver();
        notificationReceiver.setNotification(notification);
        notificationReceiver.setAccount(account);
        notificationReceiver.setIsRead(false);
        notificationReceiverRepo.save(notificationReceiver);
//        notificationReceiver.getNotification().getReceivers().add(notificationReceiver);
    }

    public List<NotificationDto> displayNotifications(String email) {

        Account accountVerify = accountRepo.findByEmail(email).
                orElseThrow(()-> new ResourceNotFoundException("account not found"));

        List<Notification> notifications = new ArrayList<>();

        List<NotificationReceiver> notificationsReceiver = notificationReceiverRepo.findByAccount_Email(email);

        for (NotificationReceiver notificationReceiver : notificationsReceiver) {
            notifications.add(notificationReceiver.getNotification());
        }

        return notifications.stream().map(notificationMapper::toDto).toList();

    }

    @Transactional
    public NotificationDto openNotification(String email, Long notificationId) {

        Account accountVerify = accountRepo.findByEmail(email).
                orElseThrow(()-> new ResourceNotFoundException("account not found"));

        Optional<NotificationReceiver> notificationsReceiver =
                notificationReceiverRepo.findByAccount_EmailAndNotification_Id(email, notificationId);
//
        if (notificationsReceiver.isPresent()) {

            notificationsReceiver.get().setIsRead(true);
            notificationsReceiver.get().setReadAt(LocalDateTime.now());
            Notification notification = notificationsReceiver.get().getNotification();

            return notificationMapper.toDto(notification);
        } else {
            throw new RuntimeException("You don't have permission to access this notification");
        }
    }

    public ApiResponseDto<Object> marksAsReadAllNotifications(String email) {

        Account account = accountRepo.findByEmail(email).
                orElseThrow(()-> new ResourceNotFoundException("account not found"));

        List<NotificationReceiver> notificationsReceiver = notificationReceiverRepo.findByAccount_Email(email);

        if(!notificationsReceiver.isEmpty()){
            for (NotificationReceiver notificationReceiver : notificationsReceiver){
                notificationReceiver.setIsRead(true);
                notificationReceiver.setReadAt(LocalDateTime.now());
                notificationReceiverRepo.save(notificationReceiver);
            }
            return new ApiResponseDto<>(
                    true,
                    "marked successfully",
                    null
            );
        }
        return new ApiResponseDto<>(
                false,
                "no notifications exist",
                null
        );
    }
}
