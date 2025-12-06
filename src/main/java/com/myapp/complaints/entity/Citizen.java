package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@Table(name = "citizen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {
    @Id
    private Long id;

    private String birthDate;

//    @RestResource(exported = false)
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Account account;
}
