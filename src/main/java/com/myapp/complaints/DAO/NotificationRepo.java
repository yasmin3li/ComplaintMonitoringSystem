package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.NotificationDto;
import com.myapp.complaints.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification,Long> {
}
