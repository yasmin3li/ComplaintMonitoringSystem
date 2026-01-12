package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "complaint_image")
//@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;
}

