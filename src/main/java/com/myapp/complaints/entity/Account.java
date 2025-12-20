package com.myapp.complaints.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.myapp.complaints.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Setter
@Getter
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
//    @JsonIgnore // don't display// this will make pw always null
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

//TODO: set default state
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

//TODO : add column in DB
    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private Long phoneNumber;

    private String profileImageUrl;

    @Column(nullable = false)
    private String nationalNumber;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    //    @RestResource(exported = false)
    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;



// Handling events
// TODO add later : passwordUpdatedAt = createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = AccountStatus.BANNED;//later for auth
        }
    }

// TODO later when we will process this state: update Account info
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

