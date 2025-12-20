package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "institution_governorate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionGovernorate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_governorate_id", nullable = false)
    private SectorGovernorate sectorGovernorate;
}

