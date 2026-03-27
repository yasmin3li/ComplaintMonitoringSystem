package com.myapp.complaints.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notification")
@RequiredArgsConstructor
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private LocalDateTime createdAt ;

    @ManyToOne
    @JoinColumn(name = "related_complaint_id", nullable = false)
    private Complaint complaint;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
