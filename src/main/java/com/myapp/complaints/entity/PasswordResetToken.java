package com.myapp.complaints.entity;


import com.myapp.complaints.enums.CodeAndLinkState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE,
                    CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn( name = "account_id")
    private Account account;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private String type;
   // private boolean isUsed;

    @Enumerated(EnumType.STRING)
    private CodeAndLinkState state;
}
