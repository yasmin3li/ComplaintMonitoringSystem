package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.NotificationReceiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationReceiverRepo extends JpaRepository<NotificationReceiver,Long> {

    List<NotificationReceiver> findByAccount_EmailOrderByNotification_CreatedAtDesc(String email);

    Optional<NotificationReceiver> findByAccount_EmailAndNotification_Id(String email, Long notificationId);

    Optional<NotificationReceiver> findByNotification_Id(Long id);

    long countByAccount_Email(String email);

    long countByAccount_EmailAndIsReadFalse(String email);
}
