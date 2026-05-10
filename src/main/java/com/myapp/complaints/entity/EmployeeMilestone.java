package com.myapp.complaints.entity;

import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestone")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false,unique = true)
    private Employee employee;

    private Long nextMilestone;

    private LocalDateTime start ;

    @PrePersist
    private void prePersist() {
        if (nextMilestone == null) {
            nextMilestone = 2L; // Default milestone value
        }
    }

}
