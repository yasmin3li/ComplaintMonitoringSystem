package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification,Long> {
}
