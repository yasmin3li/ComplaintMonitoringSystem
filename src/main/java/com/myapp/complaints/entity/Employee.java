package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    private Long id;

//    private Boolean isManager = true;

//(optional = false) to denied create employee without account
    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Account account;

    @ManyToOne(optional = false)
    @JoinColumn(name ="institution_id")
    private Institution institution;


    //added those 1/7/2026
    @ManyToOne(optional = false)
    @JoinColumn(name = "governorate_id")
    private Governorate governorate;

    @ManyToOne(optional = false)
    @JoinColumn(name ="sector_id")
    private Sector sector;


    // Soft delete
//    public void softDelete() {
//        this.account.softDelete();
//    }

}

