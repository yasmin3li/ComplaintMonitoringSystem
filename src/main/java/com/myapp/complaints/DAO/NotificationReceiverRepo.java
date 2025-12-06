package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.NotificationReceiver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationReceiverRepo extends JpaRepository<NotificationReceiver,Long> {
}
