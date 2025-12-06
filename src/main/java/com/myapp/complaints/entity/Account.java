package com.myapp.complaints.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private String userName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String state;

    @Column(nullable = false)
    private Long phoneNumber;

    private String profileImageUrl;

    @Column(nullable = false)
    private String nationalNumber;

//    @RestResource(exported = false)
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

}

