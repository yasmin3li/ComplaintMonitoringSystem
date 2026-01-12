package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "governorate")
//@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Governorate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String name;
}

