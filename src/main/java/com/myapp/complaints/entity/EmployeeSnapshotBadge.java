package com.myapp.complaints.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.myapp.complaints.enums.BadgeLevel;
import com.myapp.complaints.enums.BadgeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employee_snapshot_badge")
public class EmployeeSnapshotBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String icon;

    @Enumerated(EnumType.STRING)
    private BadgeLevel level;

    @Enumerated(EnumType.STRING)
    private BadgeType type;

    @ManyToOne
    @JoinColumn(name = "employee_performance_id")
    @JsonBackReference
    private EmployeePerformanceSnapshot snapshot;
}
