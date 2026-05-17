package com.myapp.complaints.entity;

import com.myapp.complaints.enums.VotingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "voting",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"account_id", "complaint_id"}
                )
        }
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Voting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VotingType type;

    private LocalDateTime dateTimeOfVoting = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
