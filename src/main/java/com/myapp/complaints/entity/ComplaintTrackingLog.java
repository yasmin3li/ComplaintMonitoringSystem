package com.myapp.complaints.entity;

import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_tracking_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintTrackingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "previous_status")
    @Enumerated(EnumType.STRING)
    private ComplaintState previousState;

    @Column(name = "new_status")
    @Enumerated(EnumType.STRING)
    private ComplaintState newState;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    private String comments;

    private LocalDateTime actionDate;

    @ManyToOne
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne
    @JoinColumn(name = "action_by_employee_id")
    private Employee actionBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to_employee_id")
    private Employee assignedTo;

    @PrePersist
    protected void onCreate() {
        actionDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actionDate = LocalDateTime.now();
    }
}

