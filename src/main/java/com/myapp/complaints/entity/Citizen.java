package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@Table(name = "citizen")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {
    @Id
    private Long id;

    private String birthDate;

//    @RestResource(exported = false)
//    (optional = false) to denied create employee without account
    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Account account;
}
