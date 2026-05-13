package com.myapp.complaints.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.myapp.complaints.enums.SnapshotSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "employee_performance_snapshot",
        indexes = {
                @Index(name = "idx_perf_employee_period", columnList = "employee_account_id, period_start, period_end")
        }
        ,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_period", columnNames = {"employee_account_id", "period_start", "period_end"})
        }
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePerformanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_account_id", nullable = false)
    private Long employeeAccountId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "created_count", nullable = false)//all coming complaints during the period, regardless of assignment status
    private Integer comingCount;

    @Column(name = "assigned_count", nullable = false)//complaints that handled by the employee from assigned complaints
    private Integer handledCount;

    @Column(name = "response_rate")
    private Double responseRate;

    @Column(name = "normalized_handled")
    private Double normalizedHandled;

    @Column(name = "score")
    private Double score;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    /**
     * source: e.g.
     *     SCHEDULED,
     *     MANUAL,
     *     MILESTONE,
     *     EVENT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30)
    private SnapshotSource source;


    @OneToMany(mappedBy = "snapshot",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonManagedReference
    private List<EmployeeSnapshotBadge> badges = new ArrayList<>();

}
