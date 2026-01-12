package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "institution_sector_governorate")
//@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionSectorGovernorate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isActive;

    @ManyToOne(optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_governorate_id", nullable = false)
    private SectorGovernorate sectorGovernorate;

}

