package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee")
@Data
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

    // Soft delete
//    public void softDelete() {
//        this.account.softDelete();
//    }

}

