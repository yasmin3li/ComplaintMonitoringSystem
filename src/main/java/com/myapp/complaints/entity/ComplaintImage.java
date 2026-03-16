package com.myapp.complaints.entity;

import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
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

//    private String description;
    private String imageUrl;

    private ImageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false)
    private Account addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;
}

