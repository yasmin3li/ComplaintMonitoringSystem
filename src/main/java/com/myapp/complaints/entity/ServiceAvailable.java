package com.myapp.complaints.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service")
//@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAvailable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

//TODO: add join table with institution
//    private Boolean isActive = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
}
