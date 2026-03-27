package com.myapp.complaints.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_receiver")
@RequiredArgsConstructor
@Getter
@Setter
public class NotificationReceiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean isRead ;
    private LocalDateTime readAt;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
