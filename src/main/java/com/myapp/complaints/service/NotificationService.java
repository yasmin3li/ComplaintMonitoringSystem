package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.NotificationDto;
import com.myapp.complaints.dto.NotificationStatisticsDto;
import com.myapp.complaints.dto.SendManualNotificationDto;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.NotificationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AccountRepo accountRepo;
    private final ComplaintRepo complaintRepo;
    private final NotificationTemplateRepo notificationTemplateRepo;
    private final NotificationRepo notificationRepo;
    private final AuthorizationService authorizationService;
    private final ComplaintTracingLogRepo complaintTracingLogRepo;


    @Transactional
    public void sendNotification(Notification notification, Account account) {

        NotificationReceiver notificationReceiver = new NotificationReceiver();
        notificationReceiver.setNotification(notification);
        notificationReceiver.setAccount(account);
        notificationReceiver.setIsRead(false);
        notificationReceiverRepo.save(notificationReceiver);
//        notificationReceiver.getNotification().getReceivers().add(notificationReceiver);
    }

    @Transactional
    public void notifyUsers(Complaint complaint, String reason, List<Account> users) {

        for (Account user : users) {

            Long role = user.getRole().getId();

            Optional<NotificationTemplate> template =
                    notificationTemplateRepo.findByStateAndRoleId(complaint.getState(), role);

            if(template.isPresent()){
                String message = template.get().getMessage()
                        .replace("{title}", complaint.getTitle())
                        .replace("{reason}", reason);
                Notification notification = new Notification();
                notification.setTitle(template.get().getTitle());
                notification.setMessage(message);
                notification.setComplaint(complaint);

                notificationRepo.save(notification);

                sendNotification(notification, user);
            }
            else {
                throw new ApiException("no template for role: "+role+" and state: "+complaint.getState(),HttpStatus.BAD_REQUEST);
            }
        }
    }

    public List<NotificationDto> displayNotifications(String email) {

        List<Notification> notifications = new ArrayList<>();

        List<NotificationReceiver> notificationsReceiver = notificationReceiverRepo.findByAccount_EmailOrderByNotification_CreatedAtDesc(email);

        for (NotificationReceiver notificationReceiver : notificationsReceiver) {
            notifications.add(notificationReceiver.getNotification());
        }

        return notifications.stream().map(notificationMapper::toDto).toList();

    }

    @Transactional
    public NotificationDto openNotification(String email, Long notificationId) {

        Optional<NotificationReceiver> notificationsReceiver =
                notificationReceiverRepo.findByAccount_EmailAndNotification_Id(email, notificationId);
//
        if (notificationsReceiver.isPresent()) {

            notificationsReceiver.get().setIsRead(true);
            notificationsReceiver.get().setReadAt(LocalDateTime.now());
            Notification notification = notificationsReceiver.get().getNotification();

            return notificationMapper.toDto(notification);
        } else {
            throw new ApiException("You don't have permission to access this notification",HttpStatus.FORBIDDEN);
        }
    }

    public ApiResponseDto<Object> marksAsReadAllNotifications(String email) {

        List<NotificationReceiver> notificationsReceiver = notificationReceiverRepo.findByAccount_EmailOrderByNotification_CreatedAtDesc(email);

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
        throw  new ApiException( "no notifications exist to mark it",HttpStatus.NOT_FOUND );
    }


    public NotificationStatisticsDto getNotificationStatistics(String email) {

        return new NotificationStatisticsDto(
                notificationReceiverRepo.countByAccount_Email(email),
                notificationReceiverRepo.countByAccount_EmailAndIsReadFalse(email)
        );
    }

    @Transactional
    public ApiResponseDto<Object> sendManualNotification(
            SendManualNotificationDto dto
    ) {

        Account account;

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        Account currentAccount =
                accountRepo.findByEmailAndDeletedFalse(auth.getName())
                        .orElseThrow(() ->
                                new ApiException(
                                        "account not found",
                                        HttpStatus.NOT_FOUND
                                ));

        if(authorizationService.isTechnic() && dto.accountId()==null){
            account = complaintTracingLogRepo.findAssignedByForComplaint(dto.complaintId(), currentAccount.getId());
        }
        else{
            account =
                    accountRepo.findById(dto.accountId()).orElseThrow(() -> new ApiException("account not found", HttpStatus.NOT_FOUND));
        }

        Complaint complaint = null;

        if (dto.complaintId() != null) {

            complaint = complaintRepo.findById(dto.complaintId()).orElseThrow(() -> new ApiException("complaint not found", HttpStatus.NOT_FOUND));
        }

        Notification notification = new Notification();

        notification.setTitle(dto.title());
        notification.setMessage(dto.message());

        // optional
        notification.setComplaint(complaint);

        notificationRepo.save(notification);

        sendNotification(notification, account);

        return new ApiResponseDto<>(
                true,
                "تم ارسال الاشعار بنجاح",
                null
        );
    }

}
