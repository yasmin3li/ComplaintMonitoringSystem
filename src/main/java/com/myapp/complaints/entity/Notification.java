//package com.myapp.complaints.entity;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "notification")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Notification {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String title;
//    private String message;
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    @ManyToOne
//    @JoinColumn(name = "realated_complaint_id", nullable = false)
//    private Complaint complaint;
//}
