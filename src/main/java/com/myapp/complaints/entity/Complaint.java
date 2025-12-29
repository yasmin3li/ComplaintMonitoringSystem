package com.myapp.complaints.entity;

import com.myapp.complaints.enums.ComplaintState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "complaint")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

//TODO: edit this part at db
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private ComplaintState state ;

    private LocalDateTime dateTimeOfAdd;

    private LocalDateTime dateTimeOfSolve;

// TODO: add this column to db
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "added_by_account_id")
    private Account addedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceAvailable service;

    @ManyToOne(optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(optional = false)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplaintTrackingLog> logs = new ArrayList<>();
}

